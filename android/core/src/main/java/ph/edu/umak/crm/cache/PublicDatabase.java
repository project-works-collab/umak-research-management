package ph.edu.umak.crm.cache;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PublicItem.class}, version = 1, exportSchema = false)
public abstract class PublicDatabase extends RoomDatabase {
    public abstract PublicDao content();
}
