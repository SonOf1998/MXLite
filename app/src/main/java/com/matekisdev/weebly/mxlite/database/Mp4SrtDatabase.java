package com.matekisdev.weebly.mxlite.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Mp4FileEntity.class, RecentFileEntity.class}, version = 2, exportSchema = false)
public abstract class Mp4SrtDatabase extends RoomDatabase
{
    public abstract DatabaseAccessObject getDao();
}


