package ph.edu.umak.crm.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Public metadata only; private research and credentials never enter this table. */
@Entity(tableName = "public_content")
public final class PublicItem {
    @PrimaryKey @NonNull public final String id;
    @NonNull public final String kind;
    @NonNull public final String title;
    @NonNull public final String text;
    @NonNull public final String version;
    public final long syncedAt;
    @Nullable public final String templatePath;

    public PublicItem(@NonNull String id, @NonNull String kind, @NonNull String title,
            @NonNull String text, @NonNull String version, long syncedAt,
            @Nullable String templatePath) {
        this.id = id;
        this.kind = kind;
        this.title = title;
        this.text = text;
        this.version = version;
        this.syncedAt = syncedAt;
        this.templatePath = templatePath;
    }
}
