package marcelin.thierry.chatapp.classes;

import com.google.firebase.database.Exclude;

public class ReplyNotification {

    // Firebase
    public ReplyNotification(){}

    // Replier Phone
    private String re;
    // Reply timestamp
    private long t1;
    // Comment_id (to a post made)
    private String c;
    // Reply made id ( to a comment)
    private String r;
    // Channel_name
    private String ch;
    // Channel Image
    private String chi;
    // Post_id (originally made)
    private String id;
    // Post_made content
    private String co;
    // Post_made color
    private String col;
    // Post_made message type
    private String ty;
    // Post_made timestamp
    private long t2;
    // Post_made likesCount
    private int l;
    // Post_made commentsCount
    private int cc;
    // Post_made seenCount
    private int s;
    // Reply_seen
    private boolean se;
    // Image
    private String replyImage;
    // Get replier name
    private String replierName;


    public boolean isSe() {
        return se;
    }

    public void setSe(boolean se) {
        this.se = se;
    }

    public String getRe() {
        return re;
    }

    public void setRe(String re) {
        this.re = re;
    }

    public long getT1() {
        return t1;
    }

    public void setT1(long t1) {
        this.t1 = t1;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getChi() {
        return chi;
    }

    public void setChi(String chi) {
        this.chi = chi;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getCol() {
        return col;
    }

    public void setCol(String col) {
        this.col = col;
    }

    public String getTy() {
        return ty;
    }

    public void setTy(String ty) {
        this.ty = ty;
    }

    public long getT2() {
        return t2;
    }

    public void setT2(long t2) {
        this.t2 = t2;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public int getCc() {
        return cc;
    }

    public void setCc(int cc) {
        this.cc = cc;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }
    @Exclude
    public String getReplyImage() {
        return replyImage;
    }
    @Exclude
    public void setReplyImage(String replyImage) {
        this.replyImage = replyImage;
    }

    @Exclude
    public String getReplierName() {
        return replierName;
    }

    @Exclude
    public void setReplierName(String replierName) {
        this.replierName = replierName;
    }
}
