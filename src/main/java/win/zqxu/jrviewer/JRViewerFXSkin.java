package win.zqxu.jrviewer;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.print.Printer;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.PrintPageFormat;
import org.apache.commons.lang3.StringUtils;

import java.util.ResourceBundle;

class JRViewerFXSkin extends SkinBase<JRViewerFX> {
    private static final ResourceBundle bundle = ResourceBundle.getBundle(
            JRViewerFXSkin.class.getPackage().getName() + ".jrviewer-fx");
    private static final int PAGE_FIRST = 1;
    private static final int PAGE_PREV = 2;
    private static final int PAGE_NEXT = 3;
    private static final int PAGE_LAST = 4;
    private static final int ZOOM_FULL = 1;
    private static final int ZOOM_ACUTAL = 2;
    private static final int ZOOM_WIDTH = 3;
    private static final int ZOOM_RATIO = 4;
    private static final int ZOOM_IN = 5;
    private static final int ZOOM_OUT = 6;
    private double prefScrollBarWidth;
    private HBox buttonBox = new HBox();
    private Button saveButton = new Button();
    private Button printButton = new Button();
    private Button firstButton = new Button();
    private Button prevButton = new Button();
    private Button nextButton = new Button();
    private Button lastButton = new Button();
    private TextField pageField = new TextField();
    private TextFormatter<Integer> pageFMT;
    private Button zoomInButton = new Button();
    private Button zoomOutButton = new Button();
    private ComboBox<Integer> zoomCombo = new ComboBox<>();
    private ToggleGroup zoomToggle = new ToggleGroup();
    private ToggleButton actualButton = new ToggleButton();
    private ToggleButton fullButton = new ToggleButton();
    private ToggleButton widthButton = new ToggleButton();
    private Button emailButton = new Button();
    private ScrollPane previewScroll = new ScrollPane();
    private FlowPane previewPane = new FlowPane();
    private ImageView previewImage = new ImageView();
    private HBox statusBox = new HBox();
    private Label messageLabel = new Label();
    private Label pageLabel = new Label();
    private Label printerLabel = new Label();
    private int zoomType = ZOOM_RATIO;
    private int zoomRatio = 100;

    protected JRViewerFXSkin(JRViewerFX control) {
        super(control);
        // add children
        getChildren().add(buttonBox);
        initButtonBox();
        getChildren().add(previewScroll);
        previewScroll.setContent(previewPane);
        previewPane.getChildren().add(previewImage);
        getChildren().add(statusBox);
        initStatusBox();

        // set children attributes
        buttonBox.setSpacing(1);
        previewScroll.viewportBoundsProperty().addListener(
                (v, o, n) -> {
                    previewPane.setPrefWidth(n.getWidth());
                    previewPane.setPrefHeight(n.getHeight());
                    zoomToType(zoomType);
                });
        previewPane.setPadding(new Insets(5));
        previewPane.setAlignment(Pos.CENTER);
        previewImage.setPreserveRatio(true);
        statusBox.setSpacing(5);
        updateReport();
        updatePrinter();
        control.reportProperty().addListener((v, o, n) -> updateReport());
        control.printerProperty().addListener((v, o, n) -> updatePrinter());
    }

    private void initButtonBox() {
        buttonBox.getChildren().add(initButton(saveButton, "SAVE"));
        buttonBox.getChildren().add(initButton(printButton, "PRINT"));
        buttonBox.getChildren().add(new Separator(Orientation.VERTICAL));
        buttonBox.getChildren().add(initButton(firstButton, "FIRST_PAGE"));
        buttonBox.getChildren().add(initButton(prevButton, "PREV_PAGE"));
        buttonBox.getChildren().add(initButton(nextButton, "NEXT_PAGE"));
        buttonBox.getChildren().add(initButton(lastButton, "LAST_PAGE"));
        buttonBox.getChildren().add(initPageField());
        buttonBox.getChildren().add(new Separator(Orientation.VERTICAL));
        buttonBox.getChildren().add(initButton(zoomInButton, "ZOOM_IN"));
        buttonBox.getChildren().add(initButton(zoomOutButton, "ZOOM_OUT"));
        buttonBox.getChildren().add(initZoomCombo());
        actualButton.setToggleGroup(zoomToggle);
        fullButton.setToggleGroup(zoomToggle);
        widthButton.setToggleGroup(zoomToggle);
        buttonBox.getChildren().add(initButton(actualButton, "ZOOM_ACTUAL"));
        buttonBox.getChildren().add(initButton(fullButton, "ZOOM_FULL"));
        buttonBox.getChildren().add(initButton(widthButton, "ZOOM_WIDTH"));
        buttonBox.getChildren().add(new Separator(Orientation.VERTICAL));
        buttonBox.getChildren().add(initButton(emailButton, "EMAIL"));
    }

    private <T extends ButtonBase> T initButton(T button, String action) {
        button.setId(action);
        button.setGraphic(new ImageView(getImageURL(action)));
        button.setTooltip(new Tooltip(bundle.getString(action)));
        button.setPadding(new Insets(4, 4, 3, 4));
        button.setOnAction(event -> handleAction(event));
        return button;
    }

    private String getImageURL(String action) {
        return getClass().getResource(getImageFile(action)).toExternalForm();
    }

    private String getImageFile(String action) {
        switch (action) {
            case "SAVE":
                return "disk.png";
            case "PRINT":
                return "printer.png";
            case "FIRST_PAGE":
                return "first.png";
            case "PREV_PAGE":
                return "previous.png";
            case "NEXT_PAGE":
                return "next.png";
            case "LAST_PAGE":
                return "last.png";
            case "ZOOM_IN":
                return "zoom_in.png";
            case "ZOOM_OUT":
                return "zoom_out.png";
            case "ZOOM_ACTUAL":
                return "zoom_actual.png";
            case "ZOOM_FULL":
                return "zoom_full.png";
            case "ZOOM_WIDTH":
                return "zoom_width.png";
            case "EMAIL":
                return "email.png";
            default:
                return null;
        }
    }

    private TextField initPageField() {
        pageField.setTooltip(new Tooltip(bundle.getString("GOTO_PAGE")));
        pageField.setPrefColumnCount(5);
        pageFMT = new TextFormatter<>(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return value == null ? "1" : value.toString();
            }

            @Override
            public Integer fromString(String text) {
                if (text == null || text.isEmpty())
                    text = "1";
                int value = Integer.valueOf(text);
                int count = getPageCount();
                if (value > count) value = count;
                Platform.runLater(() -> pageField.cancelEdit());
                return value < 1 ? 1 : value;
            }
        });
        pageFMT.setValue(1);
        pageFMT.valueProperty().addListener((v, o, n) -> showPage(n));
        pageField.setTextFormatter(pageFMT);
        return pageField;
    }

    private ComboBox<Integer> initZoomCombo() {
        zoomCombo.setTooltip(new Tooltip(bundle.getString("ZOOM_RATIO")));
        zoomCombo.setEditable(true);
        zoomCombo.getEditor().setPrefColumnCount(5);
        zoomCombo.getItems().addAll(50, 75, 100, 125, 150,
                175, 200, 250, 400, 800);
        zoomCombo.setValue(100);
        zoomCombo.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return value + "%";
            }

            @Override
            public Integer fromString(String text) {
                text = text.trim().replaceAll("%$", "");
                int ratio = Integer.valueOf(text);
                if (ratio < 0) throw new RuntimeException();
                ratio = ratio < 10 ? 10 : ratio;
                return ratio > 1000 ? 1000 : ratio;
            }
        });
        zoomCombo.valueProperty().addListener((v, o, n) -> zoomToRatio(n));
        return zoomCombo;
    }

    private void initStatusBox() {
        statusBox.getChildren().add(messageLabel = new Label());
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);
        statusBox.getChildren().add(pageLabel = new Label());
        pageLabel.setMinWidth(100);
        statusBox.getChildren().add(printerLabel = new Label());
        printerLabel.setMinWidth(200);
        printerLabel.setMaxWidth(300);
        printerLabel.setGraphic(new ImageView(getImageURL("PRINT")));
    }

    private void handleAction(ActionEvent event) {
        Node source = (Node) event.getSource();
        switch (source.getId()) {
            case "SAVE":
                saveJasperReport();
                break;
            case "PRINT":
//      printJasperReport();
                break;
            case "FIRST_PAGE":
                gotoPage(PAGE_FIRST);
            case "PREV_PAGE":
                gotoPage(PAGE_PREV);
                break;
            case "NEXT_PAGE":
                gotoPage(PAGE_NEXT);
                break;
            case "LAST_PAGE":
                gotoPage(PAGE_LAST);
                break;
            case "ZOOM_IN":
                zoomToType(ZOOM_IN);
                break;
            case "ZOOM_OUT":
                zoomToType(ZOOM_OUT);
                break;
            case "ZOOM_ACTUAL":
                zoomToType(ZOOM_ACUTAL);
                break;
            case "ZOOM_FULL":
                zoomToType(ZOOM_FULL);
                break;
            case "ZOOM_WIDTH":
                zoomToType(ZOOM_WIDTH);
                break;
        }
    }

    private void saveJasperReport() {
        getSkinnable().export();
    }

    private void printJasperReport() {
        getSkinnable().printWithPrintDialog();
    }

    private void updateReport() {
        boolean empty = getPageCount() == 0;
        saveButton.setDisable(empty);
        printButton.setDisable(empty);
        zoomInButton.setDisable(empty);
        zoomOutButton.setDisable(empty);
        zoomCombo.setDisable(empty);
        actualButton.setDisable(empty);
        fullButton.setDisable(empty);
        widthButton.setDisable(empty);
        if (pageFMT.getValue() != 1) {
            gotoPage(PAGE_FIRST);
        } else {
            showPage(1);
            updatePageButtons(1);
        }
    }

    private void gotoPage(int distance) {
        int page = pageFMT.getValue();
        int count = getPageCount();
        if (distance == PAGE_FIRST) {
            page = 1;
        } else if (distance == PAGE_PREV) {
            page--;
            if (page < 1) page = 1;
        } else if (distance == PAGE_NEXT) {
            page++;
            if (page > count) page = count;
        } else {
            page = count;
        }
        pageFMT.setValue(page);
        updatePageButtons(page);
    }

    private void showPage(int page) {
        int count = getPageCount();
        String label = bundle.getString("PAGE_LABEL");
        pageLabel.setText(label + page + " / " + count);
        updatePreview(calcZoomRatio());
        zoomCombo.setValue(zoomRatio);
    }

    private void updatePageButtons(int page) {
        int count = getPageCount();
        firstButton.setDisable(page <= 1);
        prevButton.setDisable(page <= 1);
        nextButton.setDisable(page >= count);
        lastButton.setDisable(page >= count);
    }

    private void zoomToType(int zoomType) {
        int currentRatio = zoomRatio;
        this.zoomType = zoomType;
        zoomRatio = calcZoomRatio();
        if (this.zoomType > ZOOM_RATIO)
            this.zoomType = ZOOM_RATIO;
        if (currentRatio != zoomRatio) {
            updatePreview(zoomRatio);
            zoomCombo.setValue(zoomRatio);
        }
    }

    private int calcZoomRatio() {
        if (getPageCount() == 0) return zoomRatio;
        Insets insets1 = previewScroll.getInsets(), insets2 = previewPane.getInsets();
        double width = previewScroll.getWidth() - insets1.getLeft() - insets1.getRight()
                - insets2.getLeft() - insets2.getRight();
        double height = previewScroll.getHeight() - insets1.getTop() - insets1.getBottom()
                - insets2.getTop() - insets2.getBottom();
        JasperPrint print = getSkinnable().getReport();
        int index = pageFMT.getValue() - 1;
        PrintPageFormat format = print.getPageFormat(index);
        int wRatio = (int) Math.floor(width * 100 / format.getPageWidth());
        int hRatio = (int) Math.floor(height * 100 / format.getPageHeight());
        ObservableList<Integer> ratioList = zoomCombo.getItems();
        switch (zoomType) {
            case ZOOM_FULL:
                return Math.min(wRatio, hRatio);
            case ZOOM_ACUTAL:
                return 100;
            case ZOOM_WIDTH:
                int maxHeight = (int) Math.ceil(format.getPageHeight() * wRatio / 100.0);
                if (maxHeight <= height) return wRatio;
                width -= getPrefScrollBarWidth();
                return (int) Math.floor(width * 100 / format.getPageWidth());
            case ZOOM_IN:
                for (int i = 0; i < ratioList.size(); i++) {
                    if (ratioList.get(i) > zoomRatio) return ratioList.get(i);
                }
                return ratioList.get(ratioList.size() - 1);
            case ZOOM_OUT:
                for (int i = ratioList.size() - 1; i >= 0; i--) {
                    if (ratioList.get(i) < zoomRatio) return ratioList.get(i);
                }
                return ratioList.get(0);
            default:
                return zoomRatio;
        }
    }

    private double getPrefScrollBarWidth() {
        if (prefScrollBarWidth == 0) {
            ScrollBar bar = new ScrollBar();
            bar.setOrientation(Orientation.VERTICAL);
            Stage stage = new Stage();
            stage.setScene(new Scene(bar));
            try {
                stage.show();
                prefScrollBarWidth = bar.prefWidth(-1);
            } finally {
                stage.close();
            }
        }
        return prefScrollBarWidth;
    }

    private void zoomToRatio(int zoomRatio) {
        if (this.zoomRatio != zoomRatio) {
            updatePreview(zoomRatio);
            zoomType = ZOOM_RATIO;
            zoomToggle.selectToggle(null);
        }
    }

    private void updatePreview(int zoomRatio) {
        this.zoomRatio = zoomRatio;
        int count = getPageCount();
        if (count == 0) {
            previewImage.setImage(null);
            return;
        }
        int pageIndex = pageFMT.getValue() - 1;
        float zoom = zoomRatio / 100.0f;
        previewImage.setImage(getSkinnable().printPageToImage(pageIndex, zoom));
    }

    private int getPageCount() {
        JasperPrint print = getSkinnable().getReport();
        return print == null ? 0 : print.getPages().size();
    }

    private void updatePrinter() {
        Printer printer = getSkinnable().getPrinter();
        if (printer == null){
            printer = Printer.getDefaultPrinter();
        }
        System.out.println("DEFAULT PRINTER:" + printer);
        String default_printer = "DEFAULT PRINTER";
        printerLabel.setText(printer!=null? StringUtils.defaultIfBlank(printer.getName(), default_printer): default_printer);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double bh = buttonBox.prefHeight(w), sh = statusBox.prefHeight(w);
        double ph = h - bh - sh - 10;
        layoutInArea(buttonBox, x, y, w, bh, 0, Insets.EMPTY, true, false, HPos.LEFT, VPos.TOP);
        y += bh + 5;
        layoutInArea(previewScroll, x, y, w, ph, 0, Insets.EMPTY, true, true, HPos.LEFT, VPos.TOP);
        y += ph + 5;
        layoutInArea(statusBox, x, y, w, sh, 0, Insets.EMPTY, true, false, HPos.LEFT, VPos.TOP);
    }

    @Override
    protected double computePrefWidth(double h, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        double bw = buttonBox.prefWidth(-1);
        double pw = previewScroll.prefWidth(-1);
        double sw = statusBox.prefWidth(-1);
        return Math.max(bw, Math.max(pw, sw)) + leftInset + rightInset;
    }

    @Override
    protected double computePrefHeight(double w, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        double bh = buttonBox.prefHeight(-1);
        double ph = previewScroll.prefHeight(-1);
        double sh = statusBox.prefHeight(-1);
        return bh + ph + sh + 10 + topInset + bottomInset;
    }

    public Button getEmailButton() {
        return emailButton;
    }

    public Button getPrintButton() {
        return printButton;
    }
}
