import entity.User;
import org.hibernate.Session;
import util.HibernateUtil;

public class Main {
    public static void main(String[] args) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();


            User admin = new User("admin", "admin123", "ADMIN");
            session.persist(admin);

            session.getTransaction().commit();
            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Starter.main(args);
    }
}