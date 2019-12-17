package com.matekisdev.weebly.mxlite.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/* MP4 fájl jelző egyed
 * A méretet és hosszt is mentjük már formázva,
 * elkerülve a futásidejű Stringműveleteket, castokat stb.
 */
@Entity(tableName = "MP4")
public class Mp4FileEntity
{
    @PrimaryKey
    @ColumnInfo(name = "PATH") @NonNull                 public String path;
    @ColumnInfo(name = "NAME")                          public String name;
    @ColumnInfo(name = "SIZE")                          public String size;
    @ColumnInfo(name = "DURATION")                      public String duration;
    @ColumnInfo(name = "HAS_SRT")                       public boolean hasSrt;
}
