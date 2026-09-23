import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Main entry point for the chat application.
 * <p>
 * This class is responsible for:
 * <ul>
 *     <li>Loading and saving data (contacts and chats).</li>
 *     <li>Building the main user interface (landing page, chat area, input bar).</li>
 *     <li>Handling user interactions such as adding/editing contacts, creating groups,
 *     opening chats, sending messages, and searching.</li>
 * </ul>
 */
public class Main extends Application {

    /** The current logged-in user of the app. In a more advanced app this would be loaded from storage. */
    private User currentUser = new User("Frances", "07123456789");

    /** True when the user is viewing a chat opened from search results. (Reserved for future use.) */
    private boolean inSearchMode = false;

    /** The last search dialog, so it can be re-shown if needed. (Reserved for future use.) */
    private Stage lastSearchStage = null;

    /**
     * JavaFX application entry point. Builds the main UI and initializes data.
     *
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        Label title = new Label(currentUser.getName());

        List<Contact> contacts = new ArrayList<>();
        List<Chat> chats = new ArrayList<>();

        // Load previously saved contacts and chats (if any)
        loadData(contacts, chats);

        // PRESET DATA (if empty)
        if (contacts.isEmpty()) {
            Contact c1 = new Contact("Alice", "0778317273");
            Contact c2 = new Contact("Bob", "0713253655");

            contacts.add(c1);
            contacts.add(c2);

            Chat chat1 = new Chat(c1.getId());
            chat1.addMessage(new Message("Alice", "Hey there!"));
            chat1.addMessage(new Message("You", "Hi!"));

            Chat chat2 = new Chat(c2.getId());
            chat2.addMessage(new Message("Bob", "Yo!"));

            chats.add(chat1);
            chats.add(chat2);
        }

        VBox chatList = new VBox(10);
        VBox messages = new VBox(10);
        ScrollPane messageScroll = new ScrollPane(messages);
        messageScroll.setFitToWidth(true);

        // Current chat is stored in a single-element array to allow modification in lambdas
        final Chat[] currentChat = new Chat[1];

        // Initial sort of chats by recency (newest last message first)
        chats.sort((a, b) -> {
            Instant ta = a.getLastMessage() == null ? Instant.MIN : a.getLastMessage().getTimeStamp();
            Instant tb = b.getLastMessage() == null ? Instant.MIN : b.getLastMessage().getTimeStamp();
            return tb.compareTo(ta);
        });

        // Populate the contact list UI
        updateContacts(contacts, chats, chatList, messages, currentChat);

        // LEFT SIDEBAR
        ScrollPane scrollPane = new ScrollPane(chatList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(300);

        // TOP BAR
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button searchBtn = new Button("🔍");
        searchBtn.setOnAction(e -> showSearchPopup(stage, contacts, chats));

        Button groupBtn = new Button("👥");
        groupBtn.setOnAction(e -> createGroup(stage, contacts, chats, chatList, messages, currentChat));

        Button addBtn = new Button("➕");
        addBtn.setOnAction(e -> addContact(stage, contacts, chats, chatList, messages, currentChat));

        Button profileBtn = new Button("👤");
        // Open profile editor dialog when clicked.
        profileBtn.setOnAction(e -> openProfileEditor());

        topBar.getChildren().addAll(title, spacer, searchBtn, groupBtn, addBtn);
        // Insert profile button after the title
        topBar.getChildren().add(1, profileBtn);

        // SORT BUTTONS
        Button sortAZ = new Button("A–Z");
        sortAZ.setOnAction(e -> {
            // Sort contacts alphabetically by name.
            contacts.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
            updateContacts(contacts, chats, chatList, messages, currentChat);
        });

        Button sortRecent = new Button("Recent");
        sortRecent.setOnAction(e -> {
            // Sort contacts by the timestamp of their last message.
            contacts.sort((c1, c2) -> {
                Chat chat1 = chats.stream()
                        .filter(ch -> ch.getContactId().equals(c1.getId()))
                        .findFirst().orElse(null);

                Chat chat2 = chats.stream()
                        .filter(ch -> ch.getContactId().equals(c2.getId()))
                        .findFirst().orElse(null);

                Instant t1 = (chat1 == null || chat1.getLastMessage() == null)
                        ? Instant.MIN
                        : chat1.getLastMessage().getTimeStamp();

                Instant t2 = (chat2 == null || chat2.getLastMessage() == null)
                        ? Instant.MIN
                        : chat2.getLastMessage().getTimeStamp();

                // Newest first
                return t2.compareTo(t1);
            });

            updateContacts(contacts, chats, chatList, messages, currentChat);
        });

        topBar.getChildren().addAll(sortAZ, sortRecent);

        // INPUT BAR
        HBox inputBar = new HBox(10);
        inputBar.setPadding(new Insets(10));

        TextField input = new TextField();
        HBox.setHgrow(input, Priority.ALWAYS);

        Button send = new Button("➤");

        // Send message when the send button is clicked
        send.setOnAction(e -> {
            String text = input.getText().trim();

            if (!text.isEmpty() && currentChat[0] != null) {
                Message msg = new Message("You", text);
                currentChat[0].addMessage(msg);

                messages.getChildren().add(createMessageBubble(msg, true, contacts, chats));
                input.clear();
                messageScroll.setVvalue(1.0);

                FileManager.saveData(contacts, chats);
                updateContacts(contacts, chats, chatList, messages, currentChat);
            }
        });

        inputBar.getChildren().addAll(input, send);

        BorderPane chatArea = new BorderPane();
        chatArea.setTop(topBar);
        chatArea.setCenter(messageScroll);
        chatArea.setBottom(inputBar);

        BorderPane root = new BorderPane();
        root.setLeft(scrollPane);
        root.setCenter(chatArea);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Loads contacts and chats from persistent storage using {@link FileManager}.
     *
     * @param contacts the list to populate with loaded contacts
     * @param chats    the list to populate with loaded chats
     */
    public void loadData(List<Contact> contacts, List<Chat> chats) {
        List<Object> data = FileManager.loadData();
        contacts.addAll((List<Contact>) data.get(0));
        chats.addAll((List<Chat>) data.get(1));
    }

    /**
     * Rebuilds the contact list UI based on the current contacts and chats.
     * <p>
     * For each contact, this method ensures there is a corresponding chat and
     * creates a visual item for the contact in the chat list.
     *
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    public void updateContacts(List<Contact> contacts, List<Chat> chats, VBox chatList,
                               VBox messages, Chat[] currentChat) {

        chatList.getChildren().clear();

        for (Contact c : contacts) {
            // Find or create a chat for this contact
            Chat chat = chats.stream()
                    .filter(ch -> ch.getContactId().equals(c.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        Chat newChat = new Chat(c.getId());
                        chats.add(newChat);
                        return newChat;
                    });

            HBox item = createChatItem(c, chat, contacts, chats, chatList, messages, currentChat);
            chatList.getChildren().add(item);
        }
    }

    /**
     * Opens a dialog that allows the user to view and edit their profile.
     * <p>
     * The dialog lets the user change their name and phone number.
     */
    private void openProfileEditor() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField(currentUser.getName());

        Label numberLabel = new Label("Phone Number:");
        TextField numberField = new TextField(currentUser.getNumber());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(numberLabel, 0, 1);
        grid.add(numberField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                // Update current user details
                currentUser.setName(nameField.getText());
                currentUser.setNumber(numberField.getText());
                return currentUser;
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Creates a visual chat item for the given contact and chat.
     * <p>
     * The item shows:
     * <ul>
     *     <li>Contact name (with group icon if applicable).</li>
     *     <li>Last message preview.</li>
     *     <li>Last message time.</li>
     *     <li>Bold name if there are unread messages.</li>
     * </ul>
     * Single-click opens the chat; double-click opens the contact profile.
     *
     * @param contact     the contact represented by this item
     * @param chat        the chat associated with the contact
     * @param contacts    the list of all contacts
     * @param chats       the list of all chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     * @return an HBox representing the chat item
     */
    private HBox createChatItem(Contact contact, Chat chat,
                                List<Contact> contacts, List<Chat> chats,
                                VBox chatList, VBox messages, Chat[] currentChat) {

        HBox box = new HBox(10);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(3);

        Label name = new Label(contact.isGroup() ? "👥 " + contact.getName() : contact.getName());

        // Last message preview + time
        Label preview = new Label(chat.getLastMessagePreview());
        Label time = new Label(chat.getLastMessageTimeFormatted());

        // Bold name if chat has unread messages
        if (chat.getHasUnread()) {
            name.setStyle("-fx-font-weight: bold;");
        }

        textBox.getChildren().addAll(name, preview);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(textBox, spacer, time);

        // CLICK → OPEN CHAT OR PROFILE
        box.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                // Double-click → open contact profile
                openContactProfile(contact, chat, contacts, chats, chatList, messages, currentChat);
                return;
            }

            // Single-click → open chat
            currentChat[0] = chat;
            messages.getChildren().clear();

            Instant lastDate = null;
            for (Message m : chat.getMessages()) {
                // Insert date separators when the day changes
                if (lastDate == null || !isSameDay(lastDate, m.getTimeStamp())) {
                    String sep = formatDateSeparator(m.getTimeStamp());
                    messages.getChildren().add(createDateSeparator(sep));
                    lastDate = m.getTimeStamp();
                }
                boolean isSent = m.getSender().equals("You");
                messages.getChildren().add(createMessageBubble(m, isSent, contacts, chats));
            }

            chat.markAllAsSeen();

            // Highlight selected chat item
            chatList.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
            box.getStyleClass().add("selected");
        });

        // RIGHT CLICK MENU
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit");
        MenuItem delete = new MenuItem("Delete");

        edit.setOnAction(e -> editContact(contact, contacts, chats, chatList, messages, currentChat));
        delete.setOnAction(e -> deleteContact(contact, contacts, chats, chatList, messages, currentChat));

        menu.getItems().addAll(edit, delete);

        box.setOnContextMenuRequested(e ->
                menu.show(box, e.getScreenX(), e.getScreenY()));

        return box;
    }

    /**
     * Opens a dialog that allows the user to add a new contact with name and phone number.
     *
     * @param owner       the owner stage
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    private void addContact(Stage owner, List<Contact> contacts, List<Chat> chats,
                            VBox chatList, VBox messages, Chat[] currentChat) {

        Dialog<Contact> dialog = new Dialog<>();
        dialog.setTitle("Add Contact");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();

        Label numberLabel = new Label("Phone Number:");
        TextField numberField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(numberLabel, 0, 1);
        grid.add(numberField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Contact(nameField.getText(), numberField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(contact -> {
            contacts.add(contact);
            chats.add(new Chat(contact.getId()));
            FileManager.saveData(contacts, chats);
            updateContacts(contacts, chats, chatList, messages, currentChat);
        });
    }

    /**
     * Opens a dialog that allows the user to edit a contact.
     * <p>
     * If the contact is a group, this method delegates to {@link #editGroup(Contact, List, List, VBox, VBox, Chat[])}.
     * Otherwise, it allows editing the contact's name and phone number.
     *
     * @param c           the contact to edit
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    private void editContact(Contact c, List<Contact> contacts, List<Chat> chats,
                             VBox chatList, VBox messages, Chat[] currentChat) {

        if (c.isGroup()) {
            editGroup(c, contacts, chats, chatList, messages, currentChat);
            return;
        }

        Dialog<Contact> dialog = new Dialog<>();
        dialog.setTitle("Edit Contact");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField(c.getName());

        Label numberLabel = new Label("Phone Number:");
        TextField numberField = new TextField(c.getNumber());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(numberLabel, 0, 1);
        grid.add(numberField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                c.setName(nameField.getText());
                c.setNumber(numberField.getText());
                return c;
            }
            return null;
        });

        dialog.showAndWait();

        FileManager.saveData(contacts, chats);
        updateContacts(contacts, chats, chatList, messages, currentChat);
    }

    /**
     * Deletes the given contact and its associated chat.
     *
     * @param c           the contact to delete
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    private void deleteContact(Contact c, List<Contact> contacts, List<Chat> chats,
                               VBox chatList, VBox messages, Chat[] currentChat) {

        contacts.remove(c);
        chats.removeIf(chat -> chat.getContactId().equals(c.getId()));

        FileManager.saveData(contacts, chats);
        updateContacts(contacts, chats, chatList, messages, currentChat);
    }

    /**
     * Opens a dialog that allows the user to edit a group contact.
     * <p>
     * The dialog allows changing the group name and adding/removing members.
     *
     * @param group       the group contact to edit
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    private void editGroup(Contact group, List<Contact> contacts, List<Chat> chats,
                           VBox chatList, VBox messages, Chat[] currentChat) {

        Dialog<Contact> dialog = new Dialog<>();
        dialog.setTitle("Edit Group");

        Label nameLabel = new Label("Group Name:");
        TextField nameField = new TextField(group.getName());

        Label membersLabel = new Label("Group Members:");

        VBox memberBox = new VBox(5);
        List<CheckBox> checkBoxes = new ArrayList<>();

        // Build a list of all non-group contacts as potential members
        for (Contact c : contacts) {
            if (c.isGroup()) continue; // groups cannot be members of groups

            CheckBox cb = new CheckBox(c.getName());
            cb.setUserData(c);

            // Pre-select existing members
            if (group.getMembers().contains(c.getId())) {
                cb.setSelected(true);
            }

            checkBoxes.add(cb);
            memberBox.getChildren().add(cb);
        }

        ScrollPane scroll = new ScrollPane(memberBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);

        VBox layout = new VBox(10, nameLabel, nameField, membersLabel, scroll);
        layout.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {

                group.setName(nameField.getText());

                // Update members
                group.getMembers().clear();
                for (CheckBox cb : checkBoxes) {
                    if (cb.isSelected()) {
                        Contact selected = (Contact) cb.getUserData();
                        group.addMember(selected);
                    }
                }

                return group;
            }
            return null;
        });

        dialog.showAndWait();

        FileManager.saveData(contacts, chats);
        updateContacts(contacts, chats, chatList, messages, currentChat);
    }

    /**
     * Opens a dialog showing the contact's profile information and
     * the three most recent messages exchanged with them.
     * <p>
     * Clicking on a recent message opens the full chat at that message.
     *
     * @param contact     the contact whose profile is being viewed
     * @param chat        the chat associated with this contact
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    private void openContactProfile(Contact contact, Chat chat,
                                    List<Contact> contacts, List<Chat> chats,
                                    VBox chatList, VBox messages, Chat[] currentChat) {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Contact Profile");

        Label nameLabel = new Label("Name: " + contact.getName());
        Label numberLabel = new Label("Phone: " + (contact.getNumber() == null ? "-" : contact.getNumber()));

        VBox recentMessagesBox = new VBox(5);
        Label recentLabel = new Label("Recent messages:");

        // Get last 3 messages (most recent first)
        List<Message> all = chat.getMessages();
        int start = Math.max(0, all.size() - 3);
        for (int i = all.size() - 1; i >= start; i--) {
            Message m = all.get(i);
            String who = m.getSender();
            Label msgLabel = new Label(who + ": " + m.getContent());
            msgLabel.setOnMouseClicked(e -> {
                // Open full chat when clicking a recent message
                openChatAtMessage(chat, m, contacts, chats, chatList, messages, currentChat);
                dialog.close();
            });
            recentMessagesBox.getChildren().add(msgLabel);
        }

        VBox layout = new VBox(10, nameLabel, numberLabel, recentLabel, recentMessagesBox);
        layout.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    /**
     * Creates a new group chat by allowing the user to specify a group name
     * and select members from the existing contacts.
     *
     * @param owner       the owner stage
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    private void createGroup(Stage owner, List<Contact> contacts, List<Chat> chats,
                             VBox chatList, VBox messages, Chat[] currentChat) {

        Dialog<Contact> dialog = new Dialog<>();
        dialog.setTitle("Create Group");

        Label nameLabel = new Label("Group Name:");
        TextField nameField = new TextField();

        Label membersLabel = new Label("Select Members:");

        VBox memberBox = new VBox(5);
        List<CheckBox> checkBoxes = new ArrayList<>();

        // All contacts (including groups) are shown; groups will be ignored when adding members
        for (Contact c : contacts) {
            CheckBox cb = new CheckBox(c.getName());
            cb.setUserData(c);
            checkBoxes.add(cb);
            memberBox.getChildren().add(cb);
        }

        ScrollPane scroll = new ScrollPane(memberBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);

        VBox layout = new VBox(10, nameLabel, nameField, membersLabel, scroll);
        layout.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Contact group = new Contact(nameField.getText());
                group.setGroup(true);

                // Add selected members
                for (CheckBox cb : checkBoxes) {
                    if (cb.isSelected()) {
                        group.addMember((Contact) cb.getUserData());
                    }
                }

                return group;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(group -> {
            contacts.add(group);
            chats.add(new Chat(group.getId()));
            FileManager.saveData(contacts, chats);
            updateContacts(contacts, chats, chatList, messages, currentChat);
        });
    }

    /**
     * Opens a popup window that allows the user to search through all messages and contacts.
     * <p>
     * The current implementation displays matching contacts and messages as labels.
     *
     * @param owner    the owner stage
     * @param contacts the list of contacts
     * @param chats    the list of chats
     */
    private void showSearchPopup(Stage owner, List<Contact> contacts, List<Chat> chats) {

        Stage popup = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TextField search = new TextField();
        VBox results = new VBox(5);

        // Live update of search results as the user types
        search.textProperty().addListener((obs, oldVal, newVal) -> {
            results.getChildren().clear();

            for (Contact c : Search.searchContacts(contacts, newVal)) {
                results.getChildren().add(new Label("👤 " + c.getName()));
            }

            for (Message m : Search.searchAllMessages(chats, newVal)) {
                results.getChildren().add(new Label("💬 " + m.getContent()));
            }
        });

        layout.getChildren().addAll(search, results);

        popup.setScene(new Scene(layout, 300, 400));
        popup.show();
    }

    /**
     * Utility method to get a contact's name from its UUID.
     *
     * @param contacts the list of contacts
     * @param id       the UUID of the contact
     * @return the contact's name, or "Unknown" if not found
     */
    private String getContactNameById(List<Contact> contacts, UUID id) {
        for (Contact c : contacts) {
            if (c.getId().equals(id)) return c.getName();
        }
        return "Unknown";
    }

    /**
     * Formats a timestamp as a time string (HH:mm).
     *
     * @param ts the timestamp to format
     * @return the formatted time string
     */
    private String formatTimestamp(Instant ts) {
        return LocalDateTime.ofInstant(ts, ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Formats a timestamp as a date separator string.
     * <p>
     * Returns "—| TODAY |—" for today, "—| YESTERDAY |—" for yesterday,
     * or "—|dd/MM/yyyy|—" for other dates.
     *
     * @param ts the timestamp to format
     * @return the formatted date separator string
     */
    private String formatDateSeparator(Instant ts) {
        LocalDate date = ts.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        if (date.equals(today)) return "—| TODAY |—";
        if (date.equals(today.minusDays(1))) return "—| YESTERDAY |—";

        return "—|" + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "|—";
    }

    /**
     * Checks whether two timestamps fall on the same calendar day.
     *
     * @param a the first timestamp
     * @param b the second timestamp
     * @return true if both timestamps are on the same day, false otherwise
     */
    private boolean isSameDay(Instant a, Instant b) {
        LocalDate da = a.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate db = b.atZone(ZoneId.systemDefault()).toLocalDate();
        return da.equals(db);
    }

    /**
     * Returns the tick symbol for a message.
     * <p>
     * Currently returns "✓✓" for both delivered and seen; the visual distinction
     * is handled via CSS (e.g., blue color for seen).
     *
     * @param msg the message
     * @return the tick symbol string
     */
    private String getTickSymbol(Message msg) {
        if (!msg.getSeen()) return "✓✓"; // delivered
        return "✓✓"; // seen (CSS colors it blue)
    }

    /**
     * Creates a date separator node for the message list.
     *
     * @param text the separator text
     * @return an HBox containing the date separator label
     */
    private HBox createDateSeparator(String text) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        Label label = new Label(text);
        label.getStyleClass().add("date-separator");

        box.getChildren().add(label);
        return box;
    }

    /**
     * Creates a message bubble UI node for the given message.
     * <p>
     * The bubble includes:
     * <ul>
     *     <li>Message text.</li>
     *     <li>Timestamp and tick symbol (for sent messages).</li>
     *     <li>Floating reaction label.</li>
     *     <li>Context menu for reactions and deletion (for sent messages).</li>
     * </ul>
     *
     * @param msg      the message to display
     * @param isSent   true if the message was sent by the current user
     * @param contacts the list of contacts
     * @param chats    the list of chats
     * @return a VBox containing the message bubble and reaction label
     */
    private VBox createMessageBubble(Message msg, boolean isSent,
                                     List<Contact> contacts, List<Chat> chats) {

        // Wrapper for bubble + floating reaction
        VBox wrapper = new VBox(2);
        wrapper.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox container = new HBox();
        container.setPadding(new Insets(5));
        container.setFillHeight(true);

        VBox bubble = new VBox(3);
        bubble.setMaxWidth(300);

        Label text = new Label(msg.getContent());
        text.setWrapText(true);

        String timeStr = formatTimestamp(msg.getTimeStamp());
        String ticks = isSent ? getTickSymbol(msg) : "";
        Label time = new Label(timeStr + "  " + ticks);
        time.getStyleClass().add("timestamp");
        time.setTextAlignment(TextAlignment.RIGHT);

        bubble.getChildren().addAll(text, time);
        bubble.getStyleClass().add("message");

        if (isSent) {
            container.setAlignment(Pos.CENTER_RIGHT);
            bubble.getStyleClass().add("sent");
        } else {
            container.setAlignment(Pos.CENTER_LEFT);
            bubble.getStyleClass().add("received");
        }

        container.getChildren().add(bubble);

        // FLOATING REACTION
        Label reactionLabel = new Label();
        reactionLabel.getStyleClass().add("reaction");

        if (msg.getReaction() != null) {
            reactionLabel.setText(msg.getReaction());
        }

        // Reaction menu
        ContextMenu reactionMenu = new ContextMenu();
        String[] reactions = {"❤", "😂", "👍", "😮", "😢", "🔥"};

        for (String r : reactions) {
            MenuItem item = new MenuItem(r);
            item.setOnAction(e -> {
                msg.setReaction(r);
                reactionLabel.setText(r);
                FileManager.saveData(contacts, chats);
            });
            reactionMenu.getItems().add(item);
        }

        // Delete menu (only for your messages)
        if (isSent) {
            MenuItem delete = new MenuItem("Delete Message");
            delete.setOnAction(e -> {
                // Remove from chat
                chats.stream()
                        .filter(ch -> ch.getMessages().contains(msg))
                        .findFirst()
                        .ifPresent(ch -> ch.getMessages().remove(msg));

                // Remove from UI
                ((VBox) wrapper.getParent()).getChildren().remove(wrapper);

                FileManager.saveData(contacts, chats);
            });
            reactionMenu.getItems().add(delete);
        }

        bubble.setOnContextMenuRequested(e ->
                reactionMenu.show(bubble, e.getScreenX(), e.getScreenY()));

        // Add bubble + floating reaction
        wrapper.getChildren().add(container);
        wrapper.getChildren().add(reactionLabel);

        return wrapper;
    }

    /**
     * Opens the given chat, displays all messages, and scrolls to the given target message.
     * <p>
     * This is used when opening a chat from the contact profile's recent messages list.
     *
     * @param chat        the chat to open
     * @param target      the message to scroll to
     * @param contacts    the list of contacts
     * @param chats       the list of chats
     * @param chatList    the VBox representing the chat list UI
     * @param messages    the VBox representing the messages UI
     * @param currentChat a single-element array holding the currently open chat
     */
    private void openChatAtMessage(Chat chat, Message target,
                                   List<Contact> contacts, List<Chat> chats,
                                   VBox chatList, VBox messages, Chat[] currentChat) {

        currentChat[0] = chat;
        messages.getChildren().clear();

        Instant lastDate = null;
        VBox targetNode = null;

        for (Message m : chat.getMessages()) {
            if (lastDate == null || !isSameDay(lastDate, m.getTimeStamp())) {
                String sep = formatDateSeparator(m.getTimeStamp());
                messages.getChildren().add(createDateSeparator(sep));
                lastDate = m.getTimeStamp();
            }

            boolean isSent = m.getSender().equals("You");
            VBox bubble = createMessageBubble(m, isSent, contacts, chats);
            messages.getChildren().add(bubble);

            if (m == target) {
                targetNode = bubble;
            }
        }

        chat.markAllAsSeen();

        // Scroll to the target message if found (focus-based)
        if (targetNode != null) {
            targetNode.requestFocus();
        }
    }
    
    public static void main(String[] args) {
        launch(args); //Starts the JavaFX runtime
    }
}