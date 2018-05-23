package win.zqxu.jrviewer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.prism.Graphics;
import com.sun.prism.j2d.J2DPrismGraphics;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.PageRange;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.PrintPageFormat;
import net.sf.jasperreports.engine.PrintPart;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleGraphics2DExporterOutput;
import net.sf.jasperreports.export.SimpleGraphics2DReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

/**
 * A Jasper report viewer written completely in JavaFx
 * 
 * <p>
 * Note: need access to <code>com.sun.javafx</code> and <code>com.sun.prism</code>
 * packages
 * </p>
 * 
 * @author zqxu
 */
@SuppressWarnings("restriction")
public class JRViewerFX extends Control {
  private static final ResourceBundle bundle = ResourceBundle.getBundle(
      JRViewerFX.class.getPackage().getName() + ".jrviewer-fx");
  private ObjectProperty<JasperPrint> report = new SimpleObjectProperty<>();
  private ObjectProperty<Printer> printer = new SimpleObjectProperty<>();
  private JasperReportsContext jasperReportsContext;

  public JRViewerFX() {
    this(null, null);
  }

  public JRViewerFX(JasperPrint report) {
    this(report, null);
  }

  public JRViewerFX(Printer printer) {
    this(null, printer);
  }

  public JRViewerFX(JasperPrint report, Printer printer) {
    setReport(report);
    setPrinter(printer);
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    return new JRViewerFXSkin(this);
  }

  public final ObjectProperty<JasperPrint> reportProperty() {
    return report;
  }

  /**
   * get report to print
   * 
   * @return the report to print
   */
  public final JasperPrint getReport() {
    return reportProperty().get();
  }

  /**
   * set report to print
   * 
   * @param report
   *          the report to print
   */
  public final void setReport(final JasperPrint report) {
    reportProperty().set(report);
  }

  public final ObjectProperty<Printer> printerProperty() {
    return printer;
  }

  /**
   * get printer used to print, null always means default printer
   * 
   * @return the printer used to print
   */
  public final Printer getPrinter() {
    return printerProperty().get();
  }

  /**
   * set printer used to print, null always means default printer
   * 
   * @param printer
   *          the printer used to print
   */
  public final void setPrinter(final Printer printer) {
    printerProperty().set(printer);
  }

  /**
   * print all pages immediately, No-OP if current no report
   */
  public void print() {
    print(new JRPrintOptions());
  }

  /**
   * print immediately with printing options, No-OP if current no report
   * 
   * @param options
   *          printing options
   */
  public void print(JRPrintOptions options) {
    JasperPrint report = getReport();
    if (report == null) return;
    PrinterJob printerJob = createPrinterJob();
    applyPrintOptions(printerJob, options);
    print(printerJob, report);
  }

  private Window getOwnerWindow() {
    Scene scene = getScene();
    return scene == null ? null : scene.getWindow();
  }

  /**
   * print with print dialog, printer change from print dialog with take effect to the
   * printer property, No-OP if current no report
   */
  public void printWithPrintDialog() {
    printWithPrintDialog(getOwnerWindow(), new JRPrintOptions());
  }

  /**
   * print with print dialog, printer change from print dialog with take effect to the
   * printer property, No-OP if current no report
   * 
   * @param owner
   *          the owner window for the print dialog
   */
  public void printWithPrintDialog(Window owner) {
    printWithPrintDialog(owner, new JRPrintOptions());
  }

  /**
   * print with print dialog, printer change from print dialog with take effect to the
   * printer property, No-OP if current no report
   * 
   * @param owner
   *          the owner window for the print dialog
   * @param defaultOptions
   *          the default printing options
   */
  public void printWithPrintDialog(Window owner, JRPrintOptions defaultOptions) {
    JasperPrint report = getReport();
    if (report == null) return;
    PrinterJob printerJob = createPrinterJob();
    applyPrintOptions(printerJob, defaultOptions);
    if (printerJob.showPrintDialog(owner)) {
      print(printerJob, report);
      setPrinter(printerJob.getPrinter());
    }
  }

  private void applyPrintOptions(PrinterJob printerJob, JRPrintOptions options) {
    int nCopies = options.getCopies();
    printerJob.getJobSettings().setCopies(nCopies < 1 ? 1 : nCopies);
    printerJob.getJobSettings().setPrintSides(options.getPrintSides());
    PageRange[] pageRanges = options.getPageRanges();
    if (pageRanges == null || pageRanges.length == 0) {
      int pageCount = getReport().getPages().size();
      pageRanges = new PageRange[]{new PageRange(1, pageCount)};
    }
    printerJob.getJobSettings().setPageRanges(pageRanges);
  }

  private boolean print(PrinterJob printerJob, JasperPrint report) {
    printerJob.getJobSettings().setJobName(report.getName());
    PageRange[] pageRanges = printerJob.getJobSettings().getPageRanges();
    int pageCount = report.getPages().size();
    boolean succeed = false;
    try {
      JasperReportsContext context = getJasperReportsContext();
      for (int page = 1; page <= pageCount; page++) {
        if (!pageInRanges(page, pageRanges)) continue;
        PageLayout layout = getPageLayout(printerJob, report, page - 1);
        JRPrintable printable = new JRPrintable(
            context, report, page - 1);
        succeed = printerJob.printPage(layout, printable);
        if (!succeed) return false;
      }
      printerJob.endJob();
    } finally {
      if (!succeed) printerJob.cancelJob();
    }
    return succeed;
  }

  private PrinterJob createPrinterJob() {
    Printer printer = getPrinter();
    return printer == null ? PrinterJob.createPrinterJob() : PrinterJob.createPrinterJob(printer);
  }

  private boolean pageInRanges(int page, PageRange[] pageRanges) {
    for (PageRange range : pageRanges) {
      if (page >= range.getStartPage() && page <= range.getEndPage())
        return true;
    }
    return false;
  }

  private PageLayout getPageLayout(PrinterJob printerJob, JasperPrint report, int pageIndex) {
    PrintPageFormat format = report.getPageFormat(pageIndex);
    Paper paper = lookupPaper(printerJob, format);
    PageOrientation orient = PageOrientation.PORTRAIT;
    if (format.getOrientation() == OrientationEnum.LANDSCAPE)
      orient = PageOrientation.LANDSCAPE;
    return printerJob.getPrinter().createPageLayout(paper, orient,
        format.getLeftMargin(), format.getRightMargin(),
        format.getTopMargin(), format.getBottomMargin());
  }

  private Paper lookupPaper(PrinterJob printerJob, PrintPageFormat format) {
    Paper current = printerJob.getJobSettings().getPageLayout().getPaper();
    double pageWidth = format.getPageWidth().doubleValue();
    double pageHeight = format.getPageHeight().doubleValue();
    if (pageWidth == current.getWidth() && pageHeight == current.getHeight())
      return current;
    Printer printer = printerJob.getPrinter();
    for (Paper paper : printer.getPrinterAttributes().getSupportedPapers()) {
      if (pageWidth == paper.getWidth() && pageHeight == current.getHeight())
        return paper;
    }
    return current;
  }

  /**
   * print specified page to image
   * 
   * @param pageIndex
   *          the page index
   * @param zoom
   *          zoom ratio
   * @return image represent specified page
   */
  public Image printPageToImage(int pageIndex, float zoom) {
    JasperPrint report = getReport();
    PrintPageFormat pageFormat = report.getPageFormat(pageIndex);
    int width = (int) Math.ceil(pageFormat.getPageWidth() * zoom);
    int height = (int) Math.ceil(pageFormat.getPageHeight() * zoom);

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(Color.white);
    graphics.fillRect(0, 0, width, height);

    try {
      JRGraphics2DExporter exporter = new JRGraphics2DExporter(getJasperReportsContext());
      exporter.setExporterInput(new SimpleExporterInput(report));
      SimpleGraphics2DExporterOutput output = new SimpleGraphics2DExporterOutput();
      output.setGraphics2D(graphics);
      exporter.setExporterOutput(output);
      SimpleGraphics2DReportConfiguration configuration = new SimpleGraphics2DReportConfiguration();
      configuration.setPageIndex(pageIndex);
      configuration.setZoomRatio(zoom);
      exporter.setConfiguration(configuration);
      exporter.exportReport();
    } catch (JRException ex) {
      throw new RuntimeException("Print page failed", ex);
    } finally {
      graphics.dispose();
    }
    return SwingFXUtils.toFXImage(image, null);
  }

  private JasperReportsContext getJasperReportsContext() {
    if (jasperReportsContext == null)
      jasperReportsContext = DefaultJasperReportsContext.getInstance();
    return jasperReportsContext;
  }

  /**
   * show export dialog, No-OP if current no report
   */
  public void export() {
    export(getOwnerWindow());
  }

  /**
   * show export dialog, No-OP if current no report
   * 
   * @param owner
   *          the owner window for export dialog
   */
  public void export(Window owner) {
    if (getReport() == null) return;
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(new File("."));
    chooser.getExtensionFilters().addAll(
        new ExtensionFilter(bundle.getString("FILTER_PDF"), "*.pdf"),
        new ExtensionFilter(bundle.getString("FILTER_HTML"), "*.html"));
    try {
      Class.forName("org.apache.poi.POIDocument"); // check POI
      chooser.getExtensionFilters().addAll(
          new ExtensionFilter(bundle.getString("FILTER_DOCX"), "*.docx"),
          new ExtensionFilter(bundle.getString("FILTER_ODT"), "*.odt"),
          new ExtensionFilter(bundle.getString("FILTER_XLS"), "*.xls"),
          new ExtensionFilter(bundle.getString("FILTER_XLSX"), "*.xlsx"),
          new ExtensionFilter(bundle.getString("FILTER_ODS"), "*.ods"),
          new ExtensionFilter(bundle.getString("FILTER_PPTX"), "*.pptx"));
    } catch (Exception ex) {
      // safety ignore this exception
    }
    File file = chooser.showSaveDialog(owner);
    if (file == null) return;
    ExtensionFilter filter = chooser.getSelectedExtensionFilter();
    String ext = filter.getExtensions().get(0).replace("*", "");
    String fileName = file.getAbsolutePath();
    if (!fileName.matches("(?i).*\\" + ext)) fileName += ext;
    export(fileName);
  }

  /**
   * export report to file, No-OP if current no report
   * 
   * @param fileName
   *          the target file name
   * @throws UnsupportedOperationException
   *           if the type determined by extension was not supported
   */
  public void export(String fileName) {
    JasperPrint report = getReport();
    if (report == null) return;
    JasperReportsContext context = getJasperReportsContext();
    try {
      switch (fileName.replaceAll(".*\\.", "").toLowerCase()) {
      case "pdf":
        JasperExportManager.exportReportToPdfFile(report, fileName);
        break;
      case "docx":
        exportToStream(new JRDocxExporter(context), report, fileName);
        break;
      case "odt":
        exportToStream(new JROdtExporter(context), report, fileName);
        break;
      case "xls":
        exportToStream(new JRXlsExporter(context), report, fileName);
        break;
      case "xlsx":
        exportToStream(new JRXlsxExporter(context), report, fileName);
        break;
      case "ods":
        exportToStream(new JROdsExporter(context), report, fileName);
        break;
      case "pptx":
        exportToStream(new JRPptxExporter(context), report, fileName);
        break;
      case "html":
        JasperExportManager.exportReportToHtmlFile(report, fileName);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported file type: " + fileName);
      }
    } catch (JRException ex) {
      throw new RuntimeException("Export report failed", ex);
    }
  }

  private void exportToStream(Exporter<ExporterInput, ?, ?, OutputStreamExporterOutput> exporter,
      JasperPrint report, String fileName) throws JRException {
    exporter.setExporterInput(new SimpleExporterInput(report));
    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName));
    exporter.exportReport();
  }

  /**
   * merge multiple reports to one report, the result report name contains first two (max)
   * report names
   * 
   * @param reports
   *          multiple reports
   * @return result report or null if reports is an empty array
   */
  public static JasperPrint merge(JasperPrint... reports) {
    if (reports == null || reports.length == 0)
      return null;
    StringBuilder builder = new StringBuilder();
    int count = 0;
    for (JasperPrint print : reports) {
      count++;
      if (count > 1) {
        builder.append(",");
      }
      if (count > 2) {
        builder.append(" ...");
        break;
      }
      builder.append(print.getName());
    }
    return merge(builder.toString(), reports);
  }

  /**
   * merge multiple reports to one report
   * 
   * @param name
   *          the result report name
   * @param reports
   *          multiple reports
   * @return result report or null if reports is an empty array
   */
  public static JasperPrint merge(String name, JasperPrint... reports) {
    if (reports == null || reports.length == 0) return null;
    JasperPrint result = new JasperPrint();
    result.setName(name);
    int pageStart = 0;
    for (JasperPrint report : reports) {
      int pageIndex = 0;
      for (JRPrintPage page : report.getPages()) {
        result.addPage(page);
        result.addPart(pageStart + pageIndex, new MergePart(report, pageIndex++));
      }
      pageStart += pageIndex;
    }
    return result;
  }

  /**
   * show preview for the report, modality WINDOW_MODAL used for non-null owner, otherwise
   * APPLICATION_MODAL used
   * 
   * @param owner
   *          the owner window for preview
   * @param report
   *          the report to preview
   */
  public static void preview(Window owner, JasperPrint report) {
    if (owner != null)
      preview(owner, Modality.WINDOW_MODAL, report);
    else
      preview(owner, Modality.APPLICATION_MODAL, report);
  }

  /**
   * show preview for the report with specified modality type
   * 
   * @param owner
   *          the owner window for preview dialog
   * @param modality
   *          the specified modality type
   * @param report
   *          the report to preview
   */
  public static void preview(Window owner, Modality modality, JasperPrint report) {
    JRViewerFX viewer = new JRViewerFX(report);
    viewer.setPadding(new Insets(8));
    Stage preview = new Stage();
    preview.initOwner(owner);
    preview.initModality(modality);
    preview.setScene(new Scene(viewer));
    preview.setTitle(bundle.getString("PREVIEW_TITLE") + report.getName());
    preview.showAndWait();
  }

  /**
   * print the report immediately to default printer
   * 
   * @param report
   *          the report to print
   */
  public static void print(JasperPrint report) {
    new JRViewerFX(report).print();
  }

  /**
   * print the report immediately to default printer with printing options
   * 
   * @param report
   *          the report to print
   * @param options
   *          the printing options
   */
  public static void print(JasperPrint report, JRPrintOptions options) {
    new JRViewerFX(report).print(options);
  }

  /**
   * print the report immediately to specified printer
   * 
   * @param report
   *          the report to print
   * @param printer
   *          the printer used to print report
   */
  public static void print(JasperPrint report, Printer printer) {
    new JRViewerFX(report, printer).print();
  }

  /**
   * print the report immediately to specified printer with printing options
   * 
   * @param report
   *          the report to print
   * @param printer
   *          the printer used to print report
   * @param options
   *          the printing options
   */
  public static void print(JasperPrint report, Printer printer, JRPrintOptions options) {
    new JRViewerFX(report, printer).print(options);
  }

  /**
   * print the report with print dialog
   * 
   * @param owner
   *          the owner window for print dialog
   * @param report
   *          the report to print
   */
  public static void printWithPrintDialog(Window owner, JasperPrint report) {
    new JRViewerFX(report).printWithPrintDialog(owner);
  }

  /**
   * print the report with print dialog and default printing options
   * 
   * @param owner
   *          the owner window for print dialog
   * @param report
   *          the report to print
   * @param defaultOptions
   *          the default printing options
   */
  public static void printWithPrintDialog(Window owner, JasperPrint report,
      JRPrintOptions defaultOptions) {
    new JRViewerFX(report).printWithPrintDialog(owner, defaultOptions);
  }

  /**
   * print the report with print dialog and default printer
   * 
   * @param owner
   *          the owner window for print dialog
   * @param report
   *          the report to print
   * @param defaultPrinter
   *          the default printer
   */
  public static void printWithPrintDialog(Window owner, JasperPrint report,
      Printer defaultPrinter) {
    new JRViewerFX(report, defaultPrinter).printWithPrintDialog(owner);
  }

  /**
   * print the report with print dialog and default printer and default printing options
   * 
   * @param owner
   *          the owner window for the print dialog
   * @param report
   *          the report to print
   * @param defaultPrinter
   *          the default printer
   * @param defaultOptions
   *          the default printing options
   */
  public static void printWithPrintDialog(Window owner, JasperPrint report,
      Printer defaultPrinter, JRPrintOptions defaultOptions) {
    new JRViewerFX(report, defaultPrinter).printWithPrintDialog(owner, defaultOptions);
  }

  /**
   * show export dialog for the report
   * 
   * @param owner
   *          the owner window for export dialog
   * @param report
   *          the report to export
   */
  public static void export(Window owner, JasperPrint report) {
    new JRViewerFX(report).export(owner);
  }

  /**
   * export the report to file
   * 
   * @param report
   *          the report to export
   * @param fileName
   *          the target file name
   */
  public static void export(JasperPrint report, String fileName) {
    new JRViewerFX(report).export(fileName);
  }

  private static class MergePart implements PrintPart {
    private String name;
    private PrintPageFormat pageFormat;

    public MergePart(JasperPrint report, int pageIndex) {
      this.name = report.getName();
      this.pageFormat = report.getPageFormat(pageIndex);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public PrintPageFormat getPageFormat() {
      return pageFormat;
    }
  }

  private static class JRPrintable extends Node {
    private JRPrintNode peer;

    public JRPrintable(JasperReportsContext jasperReportsContext,
        JasperPrint report, int pageIndex) {
      peer = new JRPrintNode(jasperReportsContext, report, pageIndex);
    }

    @Override
    protected NGNode impl_createPeer() {
      return peer;
    }

    @Override
    public BaseBounds impl_computeGeomBounds(BaseBounds bounds, BaseTransform tx) {
      return bounds;
    }

    @Override
    protected boolean impl_computeContains(double localX, double localY) {
      return false;
    }

    @Override
    public Object impl_processMXNode(MXNodeAlgorithm alg, MXNodeAlgorithmContext ctx) {
      return null;
    }
  }

  private static class JRPrintNode extends NGNode {
    private JasperReportsContext jasperReportsContext;
    private JasperPrint report;
    private int pageIndex;

    public JRPrintNode(JasperReportsContext jasperReportsContext,
        JasperPrint report, int pageIndex) {
      this.jasperReportsContext = jasperReportsContext;
      this.report = report;
      this.pageIndex = pageIndex;
    }

    @Override
    protected void renderContent(Graphics g) {
      try {
        Field field = J2DPrismGraphics.class.getDeclaredField("g2d");
        field.setAccessible(true);
        printJasperPage((Graphics2D) field.get(g));
      } catch (Exception ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Can not access Grphics2D", ex);
        throw new UnsupportedOperationException("Can not access Grphics2D", ex);
      }
    }

    private void printJasperPage(Graphics2D g2d) {
      try {
        JRGraphics2DExporter exporter = new JRGraphics2DExporter(jasperReportsContext);
        exporter.setExporterInput(new SimpleExporterInput(report));
        SimpleGraphics2DExporterOutput output = new SimpleGraphics2DExporterOutput();
        output.setGraphics2D(g2d);
        exporter.setExporterOutput(output);
        SimpleGraphics2DReportConfiguration configuration = new SimpleGraphics2DReportConfiguration();
        configuration.setPageIndex(pageIndex);
        exporter.setConfiguration(configuration);
        exporter.exportReport();
      } catch (Exception ex) {
        PrintPageFormat pageFormat = report.getPageFormat(pageIndex);
        int x = pageFormat.getLeftMargin(), y = pageFormat.getTopMargin();
        g2d.setColor(Color.BLACK);
        g2d.drawString(ex.getClass().getName() + ": " + ex.getMessage(), x, y);
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Print page failed", ex);
      }
    }

    @Override
    protected boolean hasOverlappingContents() {
      return false;
    }
  }
}
