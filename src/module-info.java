module photos {
    requires javafx.controls;
    requires javafx.fxml;

    exports photos;
    exports photos.model;
    opens photos.controller to javafx.fxml;
}
