package jp.live2d.sample;



public class Message {
    private String time;
    private String content;
    private String author;

    public Message() {
    }

    public Message(String time, String content, String author) {
        this.time = time;
        this.content = content;
        this.author = author;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}