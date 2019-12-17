package com.matekisdev.weebly.mxlite.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.matekisdev.weebly.mxlite.R;
import com.matekisdev.weebly.mxlite.VideoActivity;
import com.matekisdev.weebly.mxlite.database.DatabaseHelper;
import com.matekisdev.weebly.mxlite.database.Mp4FileEntity;
import com.matekisdev.weebly.mxlite.database.RecentFileEntity;
import com.matekisdev.weebly.mxlite.misc.FormatterFunctions;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentFilesFragment extends Fragment
{
    // Elég gáz! callback interfésszel jobb lenne
    private static ListAdapter adapter;
    public static void updateRecents()
    {
        if (adapter != null)
        {
            adapter.data.clear();
            adapter.joinedData.clear();
            new AsyncFillRecents().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private static WeakReference<TextView> noRecentWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noRecentWarning = new WeakReference<>(view.findViewById(R.id.tvNoRecentFiles));

        RecyclerView rv = view.findViewById(R.id.rvRecent);
        rv.setHasFixedSize(true);
        adapter = new ListAdapter();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        new AsyncFillRecents().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class AsyncFillRecents extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            List<RecentFileEntity> recents = DatabaseHelper.getInstance().getDb().getDao().getEveryRecentMp4File();
            adapter.data.addAll(recents);
            Collections.reverse(adapter.data);

            List<Mp4FileEntity> allFiles = DatabaseHelper.getInstance().getDb().getDao().getEveryMp4File();

            // join itt; ezért nem volt kedvem még több táblát meg entitást létrehozni
            for (int i = 0; i < adapter.data.size(); ++i)
            {
                for (int j = 0; j < allFiles.size(); ++j)
                {
                    if (adapter.data.get(i).path.equals(allFiles.get(j).path))
                    {
                        adapter.joinedData.add(allFiles.get(j));
                        break;
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();

            if (adapter.data.size() == 0 && noRecentWarning.get() != null)
            {
                noRecentWarning.get().setVisibility(View.VISIBLE);
            }
            else if (noRecentWarning.get() != null)
            {
                noRecentWarning.get().setVisibility(View.GONE);
            }
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>
    {
        private List<RecentFileEntity> data = new ArrayList<>();
        private List<Mp4FileEntity> joinedData = new ArrayList<>();

        @NonNull
        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_videos, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position)
        {
            holder.init(position, data.get(position));
        }

        @Override
        public int getItemCount()
        {
            return data.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder
        {
            int position;

            private ConstraintLayout itemItself;
            private ImageView itemPreview;
            private TextView itemName;
            private TextView itemDuration;
            private TextView itemProgress;
            private ImageView itemSrtLogo;

            void init(int position, RecentFileEntity rfe)
            {
                this.position = position;

                Glide.with(itemPreview).load(Uri.fromFile(new File(rfe.path))).into(itemPreview);

                itemName.setText(joinedData.get(position).name);
                itemDuration.setText(joinedData.get(position).duration);
                itemProgress.setText(rfe.progress);
                itemSrtLogo.setVisibility(joinedData.get(position).hasSrt ? View.VISIBLE : View.GONE);
            }

            ViewHolder(@NonNull View itemView)
            {
                super(itemView);

                itemItself = itemView.findViewById(R.id.clRItem);
                itemPreview = itemView.findViewById(R.id.ivRItemPreview);
                itemName = itemView.findViewById(R.id.tvRItemName);
                itemDuration = itemView.findViewById(R.id.tvRItemDuration);
                itemProgress = itemView.findViewById(R.id.tvRItemLastProgress);
                itemSrtLogo = itemView.findViewById(R.id.ivRItemSrtLogo);

                itemItself.setOnClickListener(v -> {

                    Intent intent = new Intent(getContext(), VideoActivity.class);
                    intent.putExtra("path", data.get(position).path);
                    intent.putExtra("hasSrt", joinedData.get(position).hasSrt);
                    intent.putExtra("progress", FormatterFunctions.dateFormatToInt(data.get(position).progress));
                    startActivity(intent);
                });
            }
        }
    }
}

