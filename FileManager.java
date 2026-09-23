import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading of application data, including
 * {@link Contact} and {@link Chat} objects.
 * <p>
 * Data is stored using Java's built-in object serialization mechanism.
 * The file {@code App.dat} contains two serialized objects in order:
 * <ol>
 *     <li>A {@code List<Contact>}</li>
 *     <li>A {@code List<Chat>}</li>
 * </ol>
 * If the file does not exist, empty lists are returned.
 */
public class FileManager {

    /** The filename used for storing serialized application data. */
    private static final String FILE_NAME = "App.dat";

    /**
     * Saves the given lists of contacts and chats to disk.
     * <p>
     * The objects are written in the following order:
     * <ol>
     *     <li>List of contacts</li>
     *     <li>List of chats</li>
     * </ol>
     *
     * @param contacts the list of contacts to save
     * @param chats    the list of chats to save
     */
    public static void saveData(List<Contact> contacts, List<Chat> chats) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(contacts);
            out.writeObject(chats);

        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Loads previously saved contacts and chats from disk.
     * <p>
     * If the file does not exist, two empty lists are returned.
     * If the file exists but cannot be read, the method prints an error
     * and returns empty lists.
     *
     * @return a list containing two elements:
     *         <ul>
     *             <li>Index 0: {@code List<Contact>}</li>
     *             <li>Index 1: {@code List<Chat>}</li>
     *         </ul>
     */
    public static List<Object> loadData() {
        List<Object> data = new ArrayList<>();

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            List<Contact> contacts = (List<Contact>) in.readObject();
            List<Chat> chats = (List<Chat>) in.readObject();

            data.add(contacts);
            data.add(chats);

        } catch (IOException e) {
            // File not found or unreadable → return empty lists
            System.out.println("No previous data found. Starting fresh.");
            data.add(new ArrayList<Contact>());
            data.add(new ArrayList<Chat>());

        } catch (ClassNotFoundException e) {
            System.out.println("Class error: " + e.getMessage());
        }

        return data;
    }
}