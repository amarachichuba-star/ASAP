import java.util.List;
import java.util.ArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides search utilities for locating contacts and messages within the application.
 * <p>
 * This class contains static methods that allow:
 * <ul>
 *     <li>Searching contacts by name or phone number</li>
 *     <li>Searching messages within a single chat</li>
 *     <li>Searching messages across all chats</li>
 * </ul>
 * All searches are case-insensitive for user convenience.
 */
public class Search {

    /**
     * Searches the list of contacts for entries whose name or phone number
     * contains the given keyword.
     * <p>
     * The search is case-insensitive for names, and exact substring matching
     * is used for phone numbers.
     *
     * @param contacts the list of contacts to search
     * @param keyword  the search keyword (may be part of a name or number)
     * @return a list of contacts matching the keyword; empty if none match
     */
    public static List<Contact> searchContacts(List<Contact> contacts, String keyword) {
        List<Contact> results = new ArrayList<>();

        if (contacts == null || keyword == null || keyword.isEmpty())
            return results;

        String lowerKeyword = keyword.toLowerCase();

        for (Contact c : contacts) {
            boolean nameMatches =
                    c.getName() != null && c.getName().toLowerCase().contains(lowerKeyword);

            boolean numberMatches =
                    c.getNumber() != null && c.getNumber().contains(keyword);

            if (nameMatches || numberMatches) {
                results.add(c);
            }
        }

        return results;
    }

    /**
     * Searches all messages within a single chat for those whose content
     * contains the given keyword.
     * <p>
     * The search is case-insensitive and matches substrings.
     *
     * @param chat    the chat whose messages should be searched
     * @param keyword the keyword to search for
     * @return a list of messages containing the keyword; empty if none match
     */
    public static List<Message> searchMessagesInChat(Chat chat, String keyword) {
        List<Message> results = new ArrayList<>();

        if (chat == null || keyword == null || keyword.isEmpty())
            return results;

        String lowerKeyword = keyword.toLowerCase();

        for (Message m : chat.getMessages()) {
            if (m.getContent() != null &&
                m.getContent().toLowerCase().contains(lowerKeyword)) {
                results.add(m);
            }
        }

        return results;
    }

    /**
     * Searches all messages across all chats for those whose content
     * contains the given keyword.
     * <p>
     * This method is used by the global search popup to locate messages
     * regardless of which chat they belong to.
     *
     * @param chats   the list of chats to search through
     * @param keyword the keyword to search for
     * @return a list of messages containing the keyword; empty if none match
     */
    public static List<Message> searchAllMessages(List<Chat> chats, String keyword) {
        List<Message> results = new ArrayList<>();

        if (chats == null || keyword == null || keyword.isEmpty())
            return results;

        String lowerKeyword = keyword.toLowerCase();

        for (Chat chat : chats) {
            for (Message m : chat.getMessages()) {
                if (m.getContent() != null &&
                    m.getContent().toLowerCase().contains(lowerKeyword)) {
                    results.add(m);
                }
            }
        }

        return results;
    }
}