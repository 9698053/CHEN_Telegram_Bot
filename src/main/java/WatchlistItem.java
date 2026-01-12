public class WatchlistItem {
    public int id;
    public long userId;
    public int itemId;
    public String type;
    public String title;
    public String status;
    public int progress;
    public Integer rating;
    public String note;

    public WatchlistItem(int id, long userId, int itemId, String type,String title, String status, int progress, Integer rating, String note) {
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.type = type;
        this.title = title;
        this.status = status;
        this.progress = progress;
        this.rating = rating;
        this.note = note;
    }
    // GETTER
    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public Integer getRating() {
        return rating;
    }

    public String getNote() {
        return note;
    }
}
