
public class User_app {

    private String nom;
    private String prenom;
    private String email;
    private String contact;
    private String pwd;
    private String photo;

    public User_app(){

    }
    public User_app (String nom_user,String prenom_user,String email_user,String pwd_user){
        this.nom = nom_user;
        this.prenom = prenom_user;
        this.email = email_user;
        this.pwd = pwd_user;
    }
}
