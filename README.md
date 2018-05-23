# jrviewer-fx
**jrviewer-fx** is a Jasper report viewer written completely in JavaFx. It has the following features:

*  View and zoom all pages of a Jasper report document
*  Print immediately with or without print dialog
*  Export report to following formats: PDF, HTML, XLS, XLSX, DOCX ... (need POI library)
*  Static methods to merge multiple reports to a single report

_Please note_
  * The **jreviewer-fx** need access to `com.sun.javafx` and `com.sun.prism` packages, this is enabled in default


# Maven dependency
```Maven
   <dependency>
     <groupId>win.zqxu</groupId>
     <artifactId>jrviewer-fx</artifactId>
     <version>latest-version</version>
   </dependency>
```
  * Current latest version is 0.1.1
  
_Please note_  
  * You need to add **jasperreports** dependency individually, this design is to avoid depends on a specific version  
  * You may also want to add **poi** dependency to support export to XLS, XLSX, DOCX, ... formats

# Known bugs
*  The print dialog can not be modality, need to upgrade Java to version 8 build 152 or later.

# How to use
The main class of **jrviewer-fx** is `win.zqxu.jrviewer.JRViewerFX`, it can be used in the following ways:
*   Call static methods to preview, print or export report, like

```Java
   JasperPrint document = (JasperPrint) JRLoader.loadObject(new File("test.jasper"));
   JRViewerFX.preview(owner, document);
   // or
   JRViewerFX.print(document);
   // or
   JRViewerFX.printWithPrintDialog(owner, document);
   // or
   JRViewerFX.export(document, "/path/to/target_file");
   // more methods please see API documents
```

```Java
   // Merge multiple reports to a single report
   JasperPrint merged = JRViewerFX.merge(report1, report2, report3, ...);
   // or
   JasperPrint merged = JRViewerFX.merge("result report name", report1, report2, report3, ...);
```

*   Create an instance of JRViewerFX and reuse it

```Java
   JRViewerFX viewer = new JRViewerFX();
   viewer.setReport((JasperPrint) JRLoader.loadObject(new File("test.jasper")));
   viewer.print();
   // or
   viewer.printWithPrintDialog();
   
   viewer.setReport((JasperPrint) JRLoader.loadObject(new File("test2.jasper")));
   viewer.print();
```

*   Import **jrviewer-fx** into JavaFX Scene Builder

    Open Scene Builder and click settings button right of search box, then choose _JAR/FXML Manager_  
    First add **jasperreports** then add **jrviewer-fx**, because the **jrviewer-fx** depends on **jasperreports**  
    You can add library from maven repository or local jar file
    
    After you import **jrviewer-fx** into Scene Builder, you can find _JRViewerFX_ in _Custom_ section  
    Just drag and drop the _JRViewerFX_ onto your pane

# International

*   Extract _/win/zqxu/jrviewer/jrviewer-fx.properties_ from _jrviewer-fx-<version>.jar_ and place it under _/win/zqxu/jrviewer_
    in your project, then rename it to _jrviewer-fx\_\<locale\>.properties_, like _jrviewer-fx\_zh\_CN.properties_,
    translate all **text** in it into your locale, that's it.

# License
*   **jrviewer-fx** itself licensed under [Eclipse Public License - v 1.0](http://www.eclipse.org/legal/epl-v10.html)
*   Icons are come from [Silk Icons](http://www.famfamfam.com/lab/icons/silk/), it licensed under [Creative Commons Attribution 2.5](http://creativecommons.org/licenses/by/2.5/)

# Version History

* version 0.1.1:

  [fix] compatible with JasperReports early version like 6.1.0 etc.
  
* version 0.1.0
  
  first version of **jrviewr-fx**
