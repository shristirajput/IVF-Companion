package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.model.ForumComment;
import com.ivf.companion.model.ForumPost;
import com.ivf.companion.model.Role;
import com.ivf.companion.model.User;
import com.ivf.companion.repository.ForumCommentRepository;
import com.ivf.companion.repository.ForumPostRepository;
import com.ivf.companion.repository.UserRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class CommunityController {

    @Autowired
    private UserSession userSession;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private UserRepository userRepository;

    // FXML Bindings - Left pane
    @FXML private ListView<ForumPost> postsListView;

    // FXML Bindings - Right pane
    @FXML private VBox postPlaceholder;
    @FXML private VBox threadWorkspace;

    @FXML private Label threadAuthorLabel;
    @FXML private Label threadDateLabel;
    @FXML private Label threadCategoryLabel;
    @FXML private Label threadTitleLabel;
    @FXML private Label threadContentLabel;

    @FXML private VBox commentsContainer;
    @FXML private TextField replyTextField;

    private ForumPost selectedPost;

    @FXML
    public void initialize() {
        // Custom Cell Rendering for Posts Directory
        postsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ForumPost item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    // Custom post summary card
                    VBox card = new VBox(8);
                    card.setPadding(new Insets(10));
                    card.setStyle("-fx-background-color: rgba(255,255,255,0.01); -fx-background-radius: 6px; -fx-cursor: hand;");

                    HBox topRow = new HBox(10);
                    topRow.setAlignment(Pos.CENTER_LEFT);

                    String authorName = item.isAnonymous() ? "Anonymous Patient" : item.getAuthor().getFullName();
                    Label authorLbl = new Label(authorName);
                    authorLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: -primary-teal; -fx-font-size: 12px;");

                    Label dateLbl = new Label(item.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd")));
                    dateLbl.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 10px;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    // Category extraction (simple search inside content or title, or mock category)
                    String categoryText = extractCategory(item);
                    Label catLbl = new Label(categoryText);
                    catLbl.setStyle("-fx-background-color: rgba(13,148,136,0.1); -fx-text-fill: -primary-teal; -fx-font-size: 9px; -fx-padding: 2px 6px; -fx-background-radius: 10px; -fx-font-weight: bold;");

                    topRow.getChildren().addAll(authorLbl, dateLbl, spacer, catLbl);

                    Label titleLbl = new Label(item.getTitle());
                    titleLbl.setWrapText(true);
                    titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

                    card.getChildren().addAll(topRow, titleLbl);
                    setGraphic(card);
                }
            }
        });

        // Setup Selection Listener
        postsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPost = newVal;
                showThreadDiscussion(newVal);
            }
        });

        // Load Posts List
        refreshPostsList();
    }

    private void refreshPostsList() {
        new Thread(() -> {
            List<ForumPost> posts = forumPostRepository.findAllByOrderByCreatedAtDesc();
            Platform.runLater(() -> postsListView.setItems(FXCollections.observableArrayList(posts)));
        }).start();
    }

    private void showThreadDiscussion(ForumPost post) {
        postPlaceholder.setVisible(false);
        threadWorkspace.setVisible(true);

        // Populate thread header
        String authorName = post.isAnonymous() ? "Anonymous Patient" : post.getAuthor().getFullName();
        threadAuthorLabel.setText("👤  " + authorName);
        threadDateLabel.setText(post.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM dd 'at' hh:mm a")));
        threadTitleLabel.setText(post.getTitle());
        threadContentLabel.setText(post.getContent());
        
        String cat = extractCategory(post);
        threadCategoryLabel.setText(cat);

        // Fetch comments
        new Thread(() -> {
            List<ForumComment> comments = forumCommentRepository.findByPostIdOrderByCreatedAtAsc(post.getId());
            Platform.runLater(() -> {
                commentsContainer.getChildren().clear();
                if (comments.isEmpty()) {
                    Label empty = new Label("No replies yet. Be the first to share support!");
                    empty.setStyle("-fx-text-fill: -text-secondary; -fx-font-style: italic; -fx-padding: 10px 0;");
                    commentsContainer.getChildren().add(empty);
                    return;
                }

                for (ForumComment comment : comments) {
                    VBox row = new VBox(6);
                    row.setStyle("-fx-background-color: rgba(255,255,255,0.01); -fx-background-radius: 8px; -fx-padding: 12px; -fx-border-color: -border-glass; -fx-border-radius: 8px;");

                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);

                    Label commentAuthor = new Label(comment.getAuthor().getFullName());
                    commentAuthor.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 12px;");

                    // Add Medical Specialist tag for doctors!
                    if (comment.getAuthor().getRole() == Role.ROLE_DOCTOR) {
                        Label tag = new Label("Specialist Doctor");
                        tag.setStyle("-fx-background-color: rgba(244,63,94,0.15); -fx-text-fill: -primary-rose; -fx-font-size: 9px; -fx-padding: 2px 6px; -fx-background-radius: 10px; -fx-font-weight: bold;");
                        header.getChildren().addAll(commentAuthor, tag);
                    } else {
                        header.getChildren().add(commentAuthor);
                    }

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label time = new Label(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd hh:mm a")));
                    time.setStyle("-fx-text-fill: -text-muted; -fx-font-size: 10px;");
                    header.getChildren().addAll(spacer, time);

                    Label body = new Label(comment.getContent());
                    body.setWrapText(true);
                    body.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 13px;");

                    row.getChildren().addAll(header, body);
                    commentsContainer.getChildren().add(row);
                }
            });
        }).start();
    }

    @FXML
    private void handlePostComment(ActionEvent event) {
        String reply = replyTextField.getText().trim();
        if (reply.isEmpty() || selectedPost == null) {
            return;
        }

        replyTextField.clear();

        new Thread(() -> {
            Optional<User> userOpt = userRepository.findById(userSession.getId());
            if (userOpt.isPresent()) {
                ForumComment comment = new ForumComment();
                comment.setPost(selectedPost);
                comment.setAuthor(userOpt.get());
                comment.setContent(reply);

                forumCommentRepository.save(comment);

                Platform.runLater(() -> showThreadDiscussion(selectedPost));
            }
        }).start();
    }

    @FXML
    private void showNewPostDialog(ActionEvent event) {
        Dialog<ForumPost> dialog = new Dialog<>();
        dialog.setTitle("Wellness Thread Creation");
        dialog.setHeaderText("Publish a new thread to the wellness community forum.");

        ButtonType postButtonType = new ButtonType("Publish Thread", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(postButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Enter post title...");

        TextArea contentArea = new TextArea();
        contentArea.setPrefHeight(120);
        contentArea.setWrapText(true);
        contentArea.setPromptText("Write your supportive thoughts or clinical query...");

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Stimulation", "Ovarian Reserve", "Emotional Support", "General");
        catBox.setValue("Stimulation");

        CheckBox anonCheckBox = new CheckBox("Post Anonymously");
        anonCheckBox.setStyle("-fx-text-fill: white; -fx-cursor: hand;");

        grid.add(new Label("Topic Category:"), 0, 0);
        grid.add(catBox, 1, 0);
        grid.add(new Label("Post Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Message Content:"), 0, 2);
        grid.add(contentArea, 1, 2);
        grid.add(new Label("Privacy Control:"), 0, 3);
        grid.add(anonCheckBox, 1, 3);

        // Apply styles
        for (javafx.scene.Node n : grid.getChildren()) {
            if (n instanceof Label) {
                n.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }
        }
        titleField.setStyle("-fx-pref-width: 300px;");
        contentArea.setStyle("-fx-pref-width: 300px;");
        catBox.setStyle("-fx-pref-width: 300px;");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #0f172a; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1px;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == postButtonType) {
                String title = titleField.getText().trim();
                String body = contentArea.getText().trim();
                if (title.isEmpty() || body.isEmpty()) {
                    return null;
                }

                // Add simple category marker to text or let service handle,
                // we'll inject the category prefix in title for extraction!
                String selectedCat = catBox.getValue().toUpperCase();
                
                ForumPost post = new ForumPost();
                post.setTitle("[" + selectedCat + "] " + title);
                post.setContent(body);
                post.setAnonymous(anonCheckBox.isSelected());
                return post;
            }
            return null;
        });

        Optional<ForumPost> result = dialog.showAndWait();
        result.ifPresent(post -> {
            new Thread(() -> {
                Optional<User> userOpt = userRepository.findById(userSession.getId());
                if (userOpt.isPresent()) {
                    post.setAuthor(userOpt.get());
                    forumPostRepository.save(post);
                    Platform.runLater(() -> {
                        refreshPostsList();
                        
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Published");
                        alert.setHeaderText(null);
                        alert.setContentText("Thread published successfully to the forum!");
                        alert.showAndWait();
                    });
                }
            }).start();
        });
    }

    private String extractCategory(ForumPost post) {
        if (post.getTitle() != null && post.getTitle().startsWith("[")) {
            int closingIdx = post.getTitle().indexOf("]");
            if (closingIdx > 1) {
                return post.getTitle().substring(1, closingIdx);
            }
        }
        return "GENERAL";
    }
}
