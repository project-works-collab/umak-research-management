package ph.edu.umak.crm.cache;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;

@Dao
public abstract class PublicDao {
    @Query("SELECT * FROM public_content ORDER BY id")
    public abstract List<PublicItem> list();

    @Query("DELETE FROM public_content")
    public abstract void clear();

    @Insert
    public abstract void insert(List<PublicItem> rows);

    @Transaction
    public void replace(List<PublicItem> rows) {
        clear();
        insert(rows);
    }
}
