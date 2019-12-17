package com.matekisdev.weebly.mxlite.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/* Itt tároljuk majd a nemrég megnyitott fájlokat
 * A sharedPreferenceshez ez túl nagy volt.
 * Áll egy autogenerált id-ből és egy mp4 fájlra való hivatkozásból.
 * A hivatkozás külső kulcs, így a fájl törlésekor a recent-ből
 * is eltűnik.
 */
@Entity(tableName = "RECENTLY_OPENED",
    foreignKeys = { @ForeignKey(entity = Mp4FileEntity.class, parentColumns = "PATH", childColumns = "PATH", onDelete = ForeignKey.CASCADE)},
    indices = { @Index(value = "PATH")}
)
public class RecentFileEntity
{
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ID") @NonNull      public int dummyId;
    @ColumnInfo(name = "PATH")                                              public String path;
    @ColumnInfo(name = "PROGRESS")                                          public String progress;
}
