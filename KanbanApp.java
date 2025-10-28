package assignment5;

//KanbanApp.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class KanbanApp extends Application {

 // Serializable model for a card
 public static class KanbanCard implements Serializable {
     private static final long serialVersionUID = 1L;
     public String id;
     public String title;
     public String description;

     public KanbanCard(String id, String title, String description) {
         this.id = id;
         this.title = title;
         this.description = description;
     }
 }

 // File to persist board state
 private static final File SAVE_FILE = new File("kanban_state.dat");

 // Model: lists for each column
 private final List<KanbanCard> todo = new ArrayList<>();
 private final List<KanbanCard> inProgress = new ArrayList<>();
 private final List<KanbanCard> done = new ArrayList<>();

 // Maps id -> UI node for quick access
 private final Map<String, Region> cardNodeMap = new HashMap<>();

 // Column VBoxes
 private VBox todoBox, inProgressBox, doneBox;

 public static void main(String[] args) {
     launch(args);
 }

 @Override
 public void start(Stage primaryStage) {
     // Try load state
     loadState();

     todoBox = makeColumn("To Do", todo);
     inProgressBox = makeColumn("In Progress", inProgress);
     doneBox = makeColumn("Done", done);

     HBox root = new HBox(20, todoBox, inProgressBox, doneBox);
     root.setPadding(new Insets(20));
     root.setStyle("-fx-background-color: #f6f9ff;");

     // Top bar with Add button and Save button
     Button addBtn = new Button("Add Card");
     addBtn.setOnAction(e -> showAddCardDialog());

     Button saveBtn = new Button("Save");
     saveBtn.setOnAction(e -> {
         saveState();
         showAlert("Saved", "Board state saved.");
     });

     HBox topbar = new HBox(10, addBtn, saveBtn);
     topbar.setPadding(new Insets(10));
     topbar.setAlignment(Pos.CENTER_LEFT);

     VBox main = new VBox(10, topbar, root);

     Scene scene = new Scene(main, 1000, 600);
     primaryStage.setTitle("Kanban Board (JavaFX) - Drag & Drop + Persist");
     primaryStage.setScene(scene);
     primaryStage.show();
 }

 // Build a column VBox with title and cards
 private VBox makeColumn(String title, List<KanbanCard> list) {
     Label lbl = new Label(title);
     lbl.setFont(Font.font(18));

     VBox column = new VBox(10);
     column.setPadding(new Insets(10));
     column.setPrefWidth(300);
     column.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d7f2; -fx-border-radius: 6; -fx-background-radius: 6;");
     column.getChildren().add(lbl);

     // allow dropping
     column.setOnDragOver(ev -> {
         if (ev.getGestureSource() != column && ev.getDragboard().hasString()) {
             ev.acceptTransferModes(TransferMode.MOVE);
         }
         ev.consume();
     });

     column.setOnDragDropped(ev -> {
         Dragboard db = ev.getDragboard();
         boolean success = false;
         if (db.hasString()) {
             String id = db.getString();
             Region node = cardNodeMap.get(id);
             if (node != null) {
                 // Remove from any parent and add to this column
                 ((Pane)node.getParent()).getChildren().remove(node);
                 column.getChildren().add(node);

                 // Update models: move card object to the correct list
                 moveModelCard(id, title);
                 success = true;
             }
         }
         ev.setDropCompleted(success);
         ev.consume();
     });

     // populate initial cards for that list
     for (KanbanCard kc : list) {
         Region cardNode = createCardNode(kc);
         column.getChildren().add(cardNode);
     }

     return column;
 }

 // Create card UI (simple HBox)
 private Region createCardNode(KanbanCard kc) {
     Label title = new Label(kc.title);
     title.setFont(Font.font(14));
     title.setWrapText(true);

     VBox content = new VBox(title);
     content.setPadding(new Insets(10));
     content.setSpacing(6);
     content.setPrefWidth(250);
     content.setStyle("-fx-background-color: #e9f0ff; -fx-border-color: #c7d6ff; -fx-border-radius: 4; -fx-background-radius: 4;");
     content.setUserData(kc.id); // store id

     // Start drag
     content.setOnDragDetected(ev -> {
         Dragboard db = content.startDragAndDrop(TransferMode.MOVE);
         ClipboardContent cc = new ClipboardContent();
         cc.putString(kc.id);
         db.setContent(cc);
         ev.consume();
     });

     // optional: allow double click to edit title
     content.setOnMouseClicked(ev -> {
         if (ev.getClickCount() == 2) {
             TextInputDialog d = new TextInputDialog(kc.title);
             d.setHeaderText("Edit card title");
             d.setTitle("Edit");
             d.showAndWait().ifPresent(newTitle -> {
                 kc.title = newTitle;
                 title.setText(newTitle);
             });
         }
     });

     cardNodeMap.put(kc.id, content);
     return content;
 }

 // Move model card to the list matching columnTitle
 private void moveModelCard(String id, String columnTitle) {
     KanbanCard card = findAndRemoveCardById(id);
     if (card == null) return;
     switch (columnTitle) {
         case "To Do": todo.add(card); break;
         case "In Progress": inProgress.add(card); break;
         case "Done": done.add(card); break;
         default: todo.add(card); break;
     }
 }

 // helper: remove card from any list and return it
 private KanbanCard findAndRemoveCardById(String id) {
     for (List<KanbanCard> lst : Arrays.asList(todo, inProgress, done)) {
         for (Iterator<KanbanCard> it = lst.iterator(); it.hasNext();) {
             KanbanCard kc = it.next();
             if (kc.id.equals(id)) {
                 it.remove();
                 return kc;
             }
         }
     }
     return null;
 }

 // Dialog to add new card (default placed in To Do)
 private void showAddCardDialog() {
     Dialog<ButtonType> dialog = new Dialog<>();
     dialog.setTitle("Add Card");
     VBox box = new VBox(8);
     TextField titleField = new TextField();
     titleField.setPromptText("Title");
     TextArea desc = new TextArea();
     desc.setPromptText("Description (optional)");
     desc.setPrefRowCount(4);
     box.getChildren().addAll(new Label("Title:"), titleField, new Label("Description:"), desc);
     dialog.getDialogPane().setContent(box);
     dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
     dialog.showAndWait().ifPresent(btn -> {
         if (btn == ButtonType.OK) {
             String t = titleField.getText().trim();
             if (!t.isEmpty()) {
                 KanbanCard kc = new KanbanCard(UUID.randomUUID().toString(), t, desc.getText());
                 todo.add(kc);
                 Region node = createCardNode(kc);
                 // add to displayed todoBox
                 todoBox.getChildren().add(node);
             }
         }
     });
 }

 // Save model lists to file
 private void saveState() {
     try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
         // Save three lists
         oos.writeObject(new ArrayList<>(todo));
         oos.writeObject(new ArrayList<>(inProgress));
         oos.writeObject(new ArrayList<>(done));
     } catch (Exception ex) {
         ex.printStackTrace();
         showAlert("Error", "Failed to save: " + ex.getMessage());
     }
 }

 // Load model lists; if file absent, create demo data
 @SuppressWarnings("unchecked")
 private void loadState() {
     if (SAVE_FILE.exists()) {
         try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
             List<KanbanCard> l1 = (List<KanbanCard>) ois.readObject();
             List<KanbanCard> l2 = (List<KanbanCard>) ois.readObject();
             List<KanbanCard> l3 = (List<KanbanCard>) ois.readObject();
             todo.clear(); todo.addAll(l1);
             inProgress.clear(); inProgress.addAll(l2);
             done.clear(); done.addAll(l3);
             return;
         } catch (Exception ex) {
             ex.printStackTrace();
             // if failed, fall through to demo data
         }
     }

     // Demo data on first run or on error
     todo.clear();
     todo.add(new KanbanCard(UUID.randomUUID().toString(), "Implement drag & drop", ""));
     todo.add(new KanbanCard(UUID.randomUUID().toString(), "Add persistence", ""));
     inProgress.clear();
     done.clear();
 }

 private void showAlert(String title, String msg) {
     Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
     a.setHeaderText(title);
     a.showAndWait();
 }
}
