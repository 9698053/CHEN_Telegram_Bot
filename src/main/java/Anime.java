public class Anime {
    // Classe interna per rappresentare un anime
        public String title;
        public String synopsis;
        public Integer episodes;
        public String imageUrl;

        public Anime(String title, String synopsis, Integer episodes, String imageUrl) {
            this.title = title;
            this.synopsis = synopsis;
            this.episodes = episodes;
            this.imageUrl = imageUrl;
        }
}

