package com.matekisdev.weebly.mxlite.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.room.Room;

import com.matekisdev.weebly.mxlite.fragments.RecentFilesFragment;
import com.matekisdev.weebly.mxlite.misc.FormatterFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class DatabaseHelper
{
    private Mp4SrtDatabase db;

    private static DatabaseHelper instance;

    public static void makeInstance(Context context)
    {
        if (instance == null)
        {
            instance = new DatabaseHelper(context);
        }
    }

    public static DatabaseHelper getInstance()
    {
        if (instance == null)
        {
            Log.e("Database singleton err:", "Using of uninitialized db helper!");
        }

        return instance;
    }

    private DatabaseHelper(Context context)
    {
        db = Room.databaseBuilder(context, Mp4SrtDatabase.class, "mxlite.sqlite3").fallbackToDestructiveMigration().build();
    }

    public Mp4SrtDatabase getDb()
    {
        return db;
    }


    public void updateDatabase()
    {
        new UpdateDatabase().doWork();
    }

    public void updateIfNeeded(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences("firstUpdate", Context.MODE_PRIVATE);
        boolean requestedAtLeastOnce = sp.getBoolean("requested", false);

        if (!requestedAtLeastOnce)
        {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("requested", true);
            editor.apply();
            new UpdateDatabase().doWork();
        }
    }

    private static class UpdateDatabase
    {
        private void recursiveFileSystemTraversal(File f)
        {
            File files[] = f.listFiles();
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    recursiveFileSystemTraversal(file);
                }
                else if (file.isFile() && file.getAbsolutePath().endsWith(".mp4"))
                {
                    Mp4FileEntity entity = new Mp4FileEntity();

                    entity.path = file.getAbsolutePath();
                    entity.name = entity.path.substring(entity.path.lastIndexOf('/') + 1).replace(".mp3", "");

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(entity.path);
                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    mmr.release();
                    long durationInLong = Long.parseLong(durationStr);
                    entity.duration = FormatterFunctions.to_hhmmss((int)(durationInLong / 1000));

                    long fileSizeInBytes = file.length();
                    entity.size = FormatterFunctions.toSizeFormat(fileSizeInBytes);

                    String potentialSrtSrc = entity.path.replace(".mp4", ".srt");

                    entity.hasSrt = new File(potentialSrtSrc).exists();

                    getInstance().getDb().getDao().insertMp4File(entity);
                }
            }
        }

        void doWork()
        {
            ArrayList<Mp4FileEntity> allVideos = new ArrayList<>(getInstance().getDb().getDao().getEveryMp4File());
            for (Mp4FileEntity videoFile : allVideos)
            {
                if (!(new File(videoFile.path).exists()))
                {
                    getInstance().getDb().getDao().deleteMp4File(videoFile);
                }
            }

            recursiveFileSystemTraversal(Environment.getExternalStorageDirectory());
        }
    }

    public void deleteFile(Mp4FileEntity entity)
    {
        new AsyncDeleteFile(entity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class AsyncDeleteFile extends AsyncTask<Void, Void, Void>
    {
        private Mp4FileEntity entity;

        AsyncDeleteFile(Mp4FileEntity entity)
        {
            this.entity = entity;
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            getInstance().getDb().getDao().deleteMp4File(entity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            RecentFilesFragment.updateRecents();
        }
    }

    // teszt jelleggel volt bennt
    public void addToRecent(Mp4FileEntity entity)
    {
        new AsyncAddToRecent(entity, "0").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // video activity-ből lesz hívva, onStop esetében
    public void addToRecent(String entityPath, int timestampInInt)
    {
        Mp4FileEntity entity = null;
        String timestamp = FormatterFunctions.to_hhmmss(timestampInInt);

        try
        {
            entity = new AsyncFindEntityByPath(entityPath).execute().get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            // do nothing;
        }

        new AsyncAddToRecent(entity, timestamp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class AsyncFindEntityByPath extends AsyncTask<Void, Void, Mp4FileEntity>
    {
        String path;

        AsyncFindEntityByPath(String path)
        {
            this.path = path;
        }

        @Override
        protected Mp4FileEntity doInBackground(Void... voids)
        {
            return getInstance().getDb().getDao().getMp4FileByPath(path);
        }
    }

    /* Megnézzük, hogy a legújabbak között ott van már-e amit most megnyitottunk
     * Ha igen, akkor töröljük.
     * Beemeljük az újat, a legvégére. Defaultba ide teszi.
     * Megjelenítéskor fordítva tesszük fel a RecentFilesFragment RV-jára.
     */
    private static class AsyncAddToRecent extends AsyncTask<Void, Void, Void>
    {
        private Mp4FileEntity entity;
        private String timestamp;

        AsyncAddToRecent(Mp4FileEntity entity, String timestamp)
        {
            this.entity = entity;
            this.timestamp = timestamp;
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            boolean alreadyInRecent = false;
            List<RecentFileEntity> recentsSoFar = getInstance().getDb().getDao().getEveryRecentMp4File();
            for (RecentFileEntity rfe : recentsSoFar)
            {
                if (rfe.path.equals(entity.path))
                {
                    alreadyInRecent = true;
                    break;
                }
            }

            if (alreadyInRecent)
            {
                getInstance().getDb().getDao().deleteRecentMp4FileByPath(entity.path);
            }

            getInstance().getDb().getDao().insertRecentMp4FileByPathAndTs(entity.path, timestamp);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);

            RecentFilesFragment.updateRecents();        // recent hozzáadásánál, frissül a recents fül
        }
    }
}
