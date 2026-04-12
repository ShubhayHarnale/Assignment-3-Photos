module photos {
    requires javafx.controls;
    requires javafx.fxml;

    exports photos;
    opens photos.controller to javafx.fxml;
}
