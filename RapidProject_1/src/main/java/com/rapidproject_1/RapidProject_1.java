package com.rapidproject_1;

import com.gluonhq.charm.glisten.application.MobileApplication;
import static com.gluonhq.charm.glisten.application.MobileApplication.HOME_VIEW;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RapidProject_1 extends MobileApplication {

    public static final String BASIC_VIEW = HOME_VIEW;

    @Override
    public void init() {
        addViewFactory(BASIC_VIEW, () -> new BasicView(BASIC_VIEW));
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.TEAL.assignTo(scene);

        ((Stage) scene.getWindow()).getIcons().add(new Image(RapidProject_1.class.getResourceAsStream("/icon.png")));
    }
}
