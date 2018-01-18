package win.zqxu.jrviewer;

import javafx.print.PageRange;
import javafx.print.PrintSides;

/**
 * printing options
 * 
 * @author zqxu
 */
public class JRPrintOptions {
  private int copies;
  private PrintSides printSides;
  private PageRange[] pageRanges;

  public JRPrintOptions() {
    this(1, null, (PageRange[]) null);
  }

  public JRPrintOptions(int nCopies) {
    this(nCopies, null, (PageRange[]) null);
  }

  public JRPrintOptions(PrintSides printSides) {
    this(1, printSides, (PageRange[]) null);
  }

  public JRPrintOptions(PageRange... pageRanges) {
    this(1, null, pageRanges);
  }

  public JRPrintOptions(int nCopies, PrintSides printSides) {
    this(nCopies, printSides, (PageRange[]) null);
  }

  public JRPrintOptions(int nCopies, PageRange... pageRanges) {
    this(nCopies, null, pageRanges);
  }

  public JRPrintOptions(int nCopies, PrintSides printSides, PageRange... pageRanges) {
    this.copies = nCopies;
    this.printSides = printSides;
    this.pageRanges = pageRanges;
  }

  /**
   * @return number of copies to print
   */
  public int getCopies() {
    return copies;
  }

  /**
   * @param nCopies
   *          number of copies to print
   */
  public void setCopies(int nCopies) {
    this.copies = nCopies;
  }

  /**
   * If a printer supports it, then a job may be printed on both sides of the media
   * (paper), ie duplex printing
   * 
   * @return the duplex (side) setting
   */
  public PrintSides getPrintSides() {
    return printSides;
  }

  /**
   * Set the PrintSides property which controls duplex printing. A null value is ignored.
   * 
   * @param printSides
   *          new setting for number of sides
   */
  public void setPrintSides(PrintSides printSides) {
    this.printSides = printSides;
  }

  /**
   * The range of pages to print. null always means all pages.
   * 
   * @return null or an array as specified above
   */
  public PageRange[] getPageRanges() {
    return pageRanges;
  }

  /**
   * The range of pages to print. null always means all pages.
   * 
   * @param pageRanges
   *          null or a varargs array as specified above
   */
  public void setPageRanges(PageRange... pageRanges) {
    this.pageRanges = pageRanges;
  }
}
