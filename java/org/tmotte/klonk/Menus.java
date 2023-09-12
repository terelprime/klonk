package org.tmotte.klonk;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.tmotte.common.swang.CurrentOS;
import org.tmotte.common.swang.KeyMapper;
import org.tmotte.common.swang.MenuUtils;
import org.tmotte.klonk.config.msg.Editors;
import org.tmotte.klonk.config.msg.Setter;
import org.tmotte.klonk.config.option.FontOptions;
import org.tmotte.klonk.controller.CtrlMain;
import org.tmotte.klonk.controller.CtrlFileOther;
import org.tmotte.klonk.controller.CtrlMarks;
import org.tmotte.klonk.controller.CtrlOptions;
import org.tmotte.klonk.controller.CtrlOther;
import org.tmotte.klonk.controller.CtrlSearch;
import org.tmotte.klonk.controller.CtrlSelection;
import org.tmotte.klonk.controller.CtrlUndo;
import org.tmotte.klonk.windows.popup.FindAndReplace;

public class Menus {

  //Various instance variables:
  private Editors editors;
  private Map<JMenuItem,Editor> switchMenuToEditor=new Hashtable<>();
  private MenuUtils mu=new MenuUtils();
  private CurrentOS currentOS;
  private JPanel osxAttachPopupsTo;
  private Setter<Boolean> markStateListener=
    b->showHasMarks(b);

  // Used when sorting the Switch menu & recent menus, respectively:
  private List<Editor> switchSortSource=new ArrayList<>(20);
  private Map<JMenu, List<String>> recentSortSources=new HashMap<>();

  //Controllers:
  private CtrlMain ctrlMain;
  private CtrlMarks ctrlMarks;
  private CtrlSelection ctrlSelection;
  private CtrlUndo ctrlUndo;
  private CtrlSearch ctrlSearch;
  private CtrlOptions ctrlOptions;
  private CtrlFileOther ctrlFileOther;
  private CtrlOther ctrlOther;

  //Visual component instances:
  private JMenuBar bar=new JMenuBar();
  private JMenu
    file, fileReopen, fileOpenFromRecentDir, fileSaveToRecentDir,
    fileFave, fileOpenFromFave, fileSaveToFave,
    search, mark, switcher, undo, select, align, external, options, osxShortcuts, help;
  private JMenuItem
    fileOpen, fileSave, fileNew, fileSaveAs, fileClose, fileCloseOthers, fileCloseAll,
      fileEncrypt, filePrint,
      fileDocDirExplore, fileClipboardDoc, fileClipboardDocDir,
      fileFind, fileOpenFromDocDir, fileOpenFromList, fileOpenFromSSH, fileSaveToDocDir, fileSaveToSSH,
      fileFaveAddFile, fileFaveAddDir,
      fileExit,
    searchFind, searchReplace, searchRepeat, searchRepeatBackwards, searchGoToLine,
    switchBackToFront, switchFrontToBack, switchNextUnsaved, pSwitchNextUnsaved,
    markSet, markGoToPrevious, markGoToNext, markClearCurrent, markClearAll,
    undoUndo, undoRedo,
      undoToBeginning, undoRedoToEnd, undoToHistorySwitch, redoToHistorySwitch,
      undoClearUndos, undoClearRedos, undoClearBoth,
    selectUpperCase, selectLowerCase, selectSortLines, selectGetSelectionSize,  selectGetAsciiValues,
    weirdInsertToAlign, weirdInsertToAlignBelow, weirdBackspaceToAlign, weirdBackspaceToAlignBelow,
      weirdAlignOneRight, weirdAlignOneLeft,
    externalRunBatch,
    optionTabsAndIndents, optionLineDelimiters, optionFont, optionFontBigger, optionFontSmaller,
      optionFavorites, optionSSH,
    osxSwitch, osxOpenFrom, osxSaveTo, osxSelect, osxFavorite, osxReopen,
    helpAbout, helpShortcut;
  private JCheckBoxMenuItem undoFast, optionAutoTrim, optionWordWrap;

  // These popup menus are for osx only. All but pswitcher are "hard linked" to
  // the main menu that they derive their data from, and are automatically updated
  // with that data.
  private boolean extraPopups=false;
  private JPopupMenu pswitcher, pOpenFrom, pSaveTo, pSelect, pFavorite, pReopen;
  private FontOptions fontOptions;
  private Setter<FontOptions> fontListener=
    (FontOptions fo) -> {
      fo.getControlsFont().set(bar);
      if (extraPopups)
        //To some extent this is double coverage because it will traverse into the same
        //items, but we need it to ensure that everything gets hit:
        fo.getControlsFont().set(pswitcher, pOpenFrom, pSaveTo, pSelect, pFavorite, pReopen);
    };


  /////////////////////
  //                 //
  // PUBLIC METHODS: //
  //                 //
  /////////////////////


  /////////////////////
  // INITIALIZATION: //
  /////////////////////

  public Menus(Editors editors, CurrentOS currentOS, FontOptions initialFontOptions) {
    this.editors=editors;
    this.currentOS=currentOS;
    this.fontOptions=initialFontOptions;

    extraPopups=currentOS.isOSX;
    create();
    listen();
    fontListener.set(initialFontOptions);
  }
  public void setControllers(
       CtrlMain ctrlMain
      ,CtrlMarks ctrlMarks
      ,CtrlSelection ctrlSelection
      ,CtrlUndo ctrlUndo
      ,CtrlSearch ctrlSearch
      ,CtrlFileOther ctrlFileOther
      ,CtrlOther ctrlOther
      ,CtrlOptions ctrlOptions
    ){
    this.ctrlMain=ctrlMain;
    this.ctrlMarks=ctrlMarks;
    this.ctrlSelection=ctrlSelection;
    this.ctrlUndo=ctrlUndo;
    this.ctrlSearch=ctrlSearch;
    this.ctrlOptions=ctrlOptions;
    this.ctrlFileOther=ctrlFileOther;
    this.ctrlOther=ctrlOther;
  }
  public JMenuBar getMenuBar() {
    return bar;
  }
  /** This is a workaround for osx making it to hard to keyboard your way to a top menu. */
  public void attachPopups(JPanel pnlEditor) {
    if (extraPopups)
      osxAttachPopupsTo=pnlEditor;
  }
  public Runnable getEditorSwitchListener() {
    return () -> editorChange();
  }
  public Setter<List<String>> getRecentFileListener() {
    return (List<String> files) -> setRecentFiles(files);
  }
  public Setter<List<String>> getRecentDirListener() {
    return (List<String> dirs) -> setRecentDirs(dirs);
  }
  public Setter<List<String>> getFavoriteFileListener() {
    return (List<String> files) -> setFavoriteFiles(files);
  }
  public Setter<List<String>> getFavoriteDirListener() {
    return (List<String> dirs) -> setFavoriteDirs(dirs);
  }
  public Setter<KeyEvent> getExtraKeyListener() {
    return ke->listenToEditorKeys(ke);
  }
  public Setter<FontOptions> getFontListener() {
    return fontListener;
  }
  public Setter<Boolean> getMarkStateListener(){
    return markStateListener;
  }



  /////////////////////////////////////////////
  // RECENT & FAVORITE FILES/DIRS SAVE/OPEN: //
  /////////////////////////////////////////////

  public void setFavoriteFiles(Collection<String> startList) {
    setFavorites(startList, fileFave, pFavorite, reopenListener, 2);
    fontOptions.getControlsFont().set(fileFave, pFavorite);
  }
  public void setFavoriteDirs(Collection<String> startList) {
    setFavorites(startList, fileOpenFromFave, openFromListener, 0);
    setFavorites(startList, fileSaveToFave,   saveToListener, 0);
    fontOptions.getControlsFont().set(fileOpenFromFave, fileSaveToFave);
  }
  public void setRecentFiles(List<String> startList) {
    setRecent(startList, fileReopen, pReopen, reopenListener);
    fontOptions.getControlsFont().set(fileReopen, pReopen);
  }
  private void setRecentDirs(List<String> startList) {
    setRecent(startList, fileOpenFromRecentDir, openFromListener);
    setRecent(startList, fileSaveToRecentDir,   saveToListener);
    fontOptions.getControlsFont().set(fileOpenFromRecentDir, fileSaveToRecentDir);
  }

  public Menus setFastUndos(boolean fast) {
    undoFast.setState(fast);
    return this;
  }
  public Menus setWordWrap(boolean w) {
    optionWordWrap.setState(w);
    return this;
  }
  public Menus setAutoTrim(boolean w) {
    optionAutoTrim.setState(w);
    return this;
  }
  public void editorChange() {
    Editor e=editors.getFirst();
    showHasMarks(e.hasMarks());
    showHasFile(e.getFile()!=null);
    setSwitchMenu();
  }


  //////////////////////
  //                  //
  // PRIVATE METHODS: //
  //                  //
  //////////////////////

  private void showHasFile(boolean has) {
    if (fileDocDirExplore!=null)
      fileDocDirExplore.setEnabled(has);
    fileFaveAddFile.setEnabled(has);
    fileFaveAddDir.setEnabled(has);
    fileClipboardDoc.setEnabled(has);
    fileClipboardDocDir.setEnabled(has);
    fileOpenFromDocDir.setEnabled(has);
    fileSaveToDocDir.setEnabled(has);
  }

  private void showHasMarks(boolean has) {
    markGoToPrevious.setEnabled(has);
    markGoToNext.setEnabled(has);
    markClearCurrent.setEnabled(has);
    markClearAll.setEnabled(has);
  }

  /**
   * Doing a removeAll() call messes up our popup menus that derive from the JMenu
   * so we preserve the title JLabel & separator here.
   */
  private void cleanMenu(JMenu menuX, JPopupMenu menuP) {
    Component[] cs=
      menuP==null
        ?null
        :menuP.getComponents();
    menuX.removeAll();
    if (menuP!=null)
      for (int i=0; i<cs.length && i<2; i++)
        menuP.add(cs[i]);
  }
  private void setRecent(List<String> startList, JMenu menuX, ActionListener listener) {
    setRecent(startList, menuX, null, listener);
  }
  private void setRecent(List<String> startList, JMenu menuX, JPopupMenu menuP, ActionListener listener) {
    int size=startList.size();
    cleanMenu(menuX, menuP);
    menuX.setEnabled(size>0);
    int easyLen=3;

    //Build first menu, a quick-list of recent files:
    for (int i=0; i<Math.min(easyLen, size); i++)
      menuX.add(mu.doMenuItem(startList.get(i), listener));

    //Build second menu, a longer list of all files you have open:
    if (size>easyLen){

      // Build & sort list of names:
      List<String> list=recentSortSources.get(menuX);
      if (list==null) {
        list=new ArrayList<>(16);
        recentSortSources.put(menuX, list);
      }
      list.clear();
      for (String s: startList)
        list.add(s);
      Collections.sort(list);

      // Now add them:
      menuX.addSeparator();
      for (String s: list)
        menuX.add(mu.doMenuItem(s, listener));
    }
  }
  private void setFavorites(
      Collection<String> startList, JMenu menuX, ActionListener listener, int skipLast
    ) {
    setFavorites(startList, menuX, null, listener, skipLast);
  }
  private void setFavorites(
      Collection<String> startList, JMenu menuX, JPopupMenu menuP, ActionListener listener, int skipLast
    ) {

    // Collect a list of items to put back:
    JMenuItem[] skip=null;
    if (skipLast>0) {
      skip=new JMenuItem[skipLast];
      int count=menuX.getItemCount();
      int start=count-skipLast;
      for (int i=start; i<count; i++)
        skip[i-start]=menuX.getItem(i);
    }

    cleanMenu(menuX, menuP);

    // Add everything:
    for (String s: startList) {
      menuX.add(mu.doMenuItem(s, listener));
    }

    // Put things back or enable/disable if empty:
    if (skipLast>0){
      menuX.addSeparator();
      for (JMenuItem i: skip)
        menuX.add(i);
    }
    else
      menuX.setEnabled(menuX.getItemCount()>0);

    if (menuP!=null)
      fontOptions.getControlsFont().set(menuP);
  }

  //////////////////
  // SWITCH MENU: //
  //////////////////

  private void setSwitchMenu() {
    switcher.removeAll();
    switchMenuToEditor.clear();
    int easyLen=3;

    // Build first menu, a quick/short list of recent files:
    int i=-1, emin=Math.min(easyLen, editors.size());
    for (Editor e: editors.forEach()){
      ++i;
      if (i>=emin)
        break;
      makeSwitchMenuItem(switcher,  e, i==0, i==1, -1);
    }

    // Build second menu (containing all items) and/or popup switcher menu:
    boolean secondSet=editors.size()>emin;
    if (secondSet)
      switcher.addSeparator();
    if (secondSet || pswitcher!=null){

      // Rebuild sorted list of editors:
      switchSortSource.clear();
      for (Editor e: editors.forEach())
        switchSortSource.add(e);
      Collections.sort(switchSortSource, switchSorter);

      // Clear popup of all but first 2 and last 2 items:
      // (We could do it this way this with our main switcher but never bothered)
      if (pswitcher!=null) {
        int size=pswitcher.getComponentCount();
        for (int ps=2; ps<size-2; ps++)
          pswitcher.remove(2);
      }

      // Add to whichever menu needs it. Be careful: You must
      // make a new menu item each time, EVEN when adding it to
      // different menus:
      Editor firstEditor=editors.getFirst();
      int count=-1;
      for (Editor e: switchSortSource) {
        count++;
        if (secondSet)
          makeSwitchMenuItem(switcher,  e, e==firstEditor, false, -1);
        if (pswitcher!=null)
          makeSwitchMenuItem(pswitcher, e, e==firstEditor, false, 2+count);
      }
    }


    //Build third menu, which swaps current for the other:
    if (editors.size()>1){
      switcher.addSeparator();
      switcher.add(switchFrontToBack);
      switcher.add(switchBackToFront);
      switcher.add(switchNextUnsaved);
    }

    // Change fonts:
    if (pswitcher!=null)
      fontOptions.getControlsFont().set(pswitcher);
    fontOptions.getControlsFont().set(switcher);

    fileCloseOthers.setEnabled(editors.size()>1);
  }
  /** Only called by setSwitchMenu() */
  private void makeSwitchMenuItem(
      javax.swing.JComponent c, Editor e, boolean checked, boolean f12, int index
    ) {
    JMenuItem jmi=
      checked
        ?mu.doMenuItemCheckbox(e.getTitle(), switchListener)
        :mu.doMenuItem(
          e.getTitle(), switchListener, -1,
          f12
            ?KeyMapper.key(KeyEvent.VK_F12,0)
            :null
        );
    switchMenuToEditor.put(jmi, e);
    if (index>0)
      c.add(jmi, index);
    else
      c.add(jmi);
  }
  /** Also only used by setSwitchMenu() */
  private static Comparator<Editor> switchSorter=new Comparator<Editor> () {
    public int compare(Editor e1, Editor e2) {
      return e1.getTitle().compareTo(e2.getTitle());
    }
  };

  ///////////////////////////
  // CREATE/LAYOUT/LISTEN: //
  ///////////////////////////

  //Unlike a lot of other create/layout/listens, this is almost all-in-one,
  //since everything is generic and we can chain function calls like an
  //absolute lunatic, which is fun.
  private void create() {
    bar.setBorderPainted(false);

    file=mu.doMenu(bar, "File", KeyEvent.VK_F);

    //FILE MENU SECTION 1:
    mu.add(
       file
      ,fileOpen =mu.doMenuItem(
        "Open...", fileListener, KeyEvent.VK_O,
        KeyMapper.keyByOS(KeyEvent.VK_O)
      )
      ,fileNew  =mu.doMenuItem(
        "New",     fileListener, KeyEvent.VK_N,
        KeyMapper.keyByOS(KeyEvent.VK_N)
      )
      ,fileSave =mu.doMenuItem(
        "Save",    fileListener, KeyEvent.VK_S,
        KeyMapper.keyByOS(KeyEvent.VK_S)
      )
      ,fileSaveAs=mu.doMenuItem(
        "Save as...",   fileListener, KeyEvent.VK_A,
        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_S, InputEvent.META_DOWN_MASK, InputEvent.SHIFT_DOWN_MASK)
          :null
      )
      ,fileClose =mu.doMenuItem(
        "Close",   fileListener, KeyEvent.VK_C,
        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_W, InputEvent.META_DOWN_MASK)
          :KeyMapper.key(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK)
      )
      ,fileCloseOthers=mu.doMenuItem(
        "Close others",   fileListener
      )
      ,fileCloseAll=mu.doMenuItem(
        "Close all",   fileListener, KeyEvent.VK_E
      )
    );

    //FILE MENU SECTION 2 (PRINT):
    file.addSeparator();
    mu.add(
       file
      ,fileEncrypt=mu.doMenuItem(
        "Encrypt", fileListener, KeyEvent.VK_T
      )
      ,filePrint=mu.doMenuItem(
        "Print", fileListener, KeyEvent.VK_P,
        KeyMapper.key(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK)
      )
    );

    //FILE MENU SECTION 3:
    file.addSeparator();
    if (currentOS.isMSWindows || currentOS.isOSX)
      file.add(fileDocDirExplore =mu.doMenuItem(
        "Open document's directory", fileListener, KeyEvent.VK_D
      ));
    mu.add(
      file,
      fileClipboardDoc=mu.doMenuItem(
        "Copy document name to clipboard", fileListener, KeyEvent.VK_Y,
        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_Y, InputEvent.META_DOWN_MASK)
          :null
      ),
      fileClipboardDocDir=mu.doMenuItem(
        "Copy document directory to clipboard", fileListener, KeyEvent.VK_U,
        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_Y, InputEvent.META_DOWN_MASK, InputEvent.SHIFT_DOWN_MASK)
          :null
      )
    );


    //FILE MENU SECTION 4:
    file.addSeparator();
    //Used for popups:
    JMenu
      fileOpenFrom=mu.doMenu("Open from", KeyEvent.VK_F),
      fileSaveTo=mu.doMenu("Save to", KeyEvent.VK_V);
    mu.add(
      file
      ,fileFind=mu.doMenuItem(
          "Find files...", fileListener, KeyEvent.VK_L
      )
      ,mu.add(
        fileOpenFrom
        ,fileOpenFromDocDir   =mu.doMenuItem(
          "Current document directory", fileListener, KeyEvent.VK_C
        )
        ,fileOpenFromRecentDir=mu.doMenu(
          "Recent directory", KeyEvent.VK_R
        )
        ,fileOpenFromFave     =mu.doMenu(
          "Favorite directory", KeyEvent.VK_F
        )
        ,fileOpenFromList     =
          currentOS.isOSX
            ?mu.doMenuItem(
              "List...", fileListener, KeyEvent.VK_F,
              KeyMapper.key(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK)
            )
            :null
        ,null
        ,fileOpenFromSSH      =mu.doMenuItem(
          "SSH...", fileListener, KeyEvent.VK_S
        )
      )
      ,mu.add(
        fileSaveTo
        ,fileSaveToDocDir=mu.doMenuItem(
          "Current document directory", fileListener, KeyEvent.VK_C
        )
        ,fileSaveToRecentDir=mu.doMenu(
          "Recent directory", KeyEvent.VK_R
        )
        ,fileSaveToFave=mu.doMenu(
          "Favorite directory", KeyEvent.VK_F
        )
        ,fileSaveToSSH=mu.doMenuItem(
          "SSH...", fileListener, KeyEvent.VK_S
        )
      )
      ,
      mu.add(
        fileFave  =mu.doMenu("Favorite files", KeyEvent.VK_I)
        ,
        fileFaveAddFile=mu.doMenuItem(
          "Add current file to favorites", fileListener, KeyEvent.VK_A
        )
        ,
        fileFaveAddDir=mu.doMenuItem(
          "Add current directory to favorites", fileListener, KeyEvent.VK_D
        )
      )
      ,
      fileReopen=mu.doMenu("Re-open", KeyEvent.VK_R)
    );
    if (extraPopups) {
      pOpenFrom=makePopup(fileOpenFrom, "Open from:");
      pSaveTo=makePopup(fileSaveTo, "Save to:");
      pFavorite=makePopup(fileFave, "Favorite files:");
      pReopen=makePopup(fileReopen, "Reopen file:");
    }

    //FILE MENU SECTION 5 (EXIT)
    file.addSeparator();
    file.add(fileExit  =mu.doMenuItem(
      "Exit",    fileListener, KeyEvent.VK_X,
      currentOS.isOSX
        ?null
        :KeyMapper.key(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK)
    ));


    //SWITCH:
    switcher=mu.doMenu(bar, "Switch",     KeyEvent.VK_W);
    switchFrontToBack=mu.doMenuItem(
      "Send front to back", switchListener, KeyEvent.VK_S,
      currentOS.isOSX
        ?KeyMapper.key(KeyEvent.VK_BACK_QUOTE, KeyMapper.shortcutByOS())
        :KeyMapper.key(KeyEvent.VK_F11)
    );
    switchBackToFront=mu.doMenuItem(
      "Send back to front", switchListener, KeyEvent.VK_E,
      currentOS.isOSX
        ?KeyMapper.key(KeyEvent.VK_BACK_QUOTE, KeyMapper.shortcutByOS(), KeyEvent.SHIFT_DOWN_MASK)
        :KeyMapper.key(KeyEvent.VK_F11, KeyEvent.SHIFT_DOWN_MASK)
    );
    switchNextUnsaved=mu.doMenuItem(
      "Next unsaved file", switchListener, KeyEvent.VK_X
    );
    if (extraPopups) {
      pswitcher=makeLabelledPopup("Switch:");
      pSwitchNextUnsaved=mu.doMenuItem(
        "Next unsaved file", switchListener
      );
      pswitcher.addSeparator();
      pswitcher.add(pSwitchNextUnsaved);
    }


    //SEARCH:
    search=mu.doMenu(bar, "Search", KeyEvent.VK_S);
    mu.add(
      search
      ,
      searchFind=mu.doMenuItem(
        "Find", searchListener, KeyEvent.VK_F,
        KeyMapper.key(KeyEvent.VK_F, KeyMapper.shortcutByOS())
      )
      ,
      searchReplace=mu.doMenuItem(
        "Replace", searchListener, KeyEvent.VK_R,
        KeyMapper.key(KeyEvent.VK_R, KeyMapper.shortcutByOS())
      )
    );
    search.addSeparator();
    mu.add(
      search
      ,
      searchRepeat=mu.doMenuItem(
        "Repeat find/replace", searchListener, KeyEvent.VK_P,
        FindAndReplace.getFindAgainKey(currentOS)
      )
      ,
      searchRepeatBackwards=mu.doMenuItem(
        "...Backwards", searchListener, KeyEvent.VK_B,
        FindAndReplace.getFindAgainReverseKey(currentOS)
      )
    );
    search.addSeparator();
    mu.add(
      search
      ,
      searchGoToLine=mu.doMenuItem(
        "Go to line number...", searchListener, KeyEvent.VK_O,
        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_T, KeyMapper.shortcutByOS())
          :KeyMapper.key(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK)
      )
    );


    //MARK:
    mark=mu.doMenu(bar, "Mark", markListener, KeyEvent.VK_M);
    mu.add(
      mark,
      markSet=mu.doMenuItem(
        "Set mark", markItemListener, KeyEvent.VK_S,
        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_M, KeyMapper.shortcutByOS())
          :KeyMapper.key(KeyEvent.VK_F4)
      )
      ,
      markGoToPrevious=mu.doMenuItem(
        "Go to previous mark", markItemListener, KeyEvent.VK_G,
        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_COMMA, KeyMapper.shortcutByOS())
          :KeyMapper.key(KeyEvent.VK_F8)
      )
      ,
      markGoToNext=mu.doMenuItem(
        "Go to next mark", markItemListener, KeyEvent.VK_O,
          currentOS.isOSX
            ?KeyMapper.key(KeyEvent.VK_PERIOD, KeyMapper.shortcutByOS())
            :KeyMapper.key(KeyEvent.VK_F9)
      )
      ,
      markClearCurrent=mu.doMenuItem(
        "Clear current mark",
        markItemListener, KeyEvent.VK_C,

        currentOS.isOSX
          ?KeyMapper.key(KeyEvent.VK_M, KeyMapper.shortcutByOS(), KeyEvent.SHIFT_DOWN_MASK)
          :KeyMapper.key(KeyEvent.VK_F4, KeyEvent.SHIFT_DOWN_MASK)
      )
      ,
      markClearAll=mu.doMenuItem(
        "Clear all marks",
        markItemListener, KeyEvent.VK_L
      )
    );
    showHasMarks(false);


    //UNDO:
    undo=mu.doMenu(bar, "Undo", KeyEvent.VK_U);
    mu.add(
      undo
      ,
      undoUndo=mu.doMenuItem(
        "Undo", undoItemListener, KeyEvent.VK_U,
        KeyMapper.keyByOS(KeyEvent.VK_Z)
      )
      ,
      undoRedo=mu.doMenuItem(
        "Redo", undoItemListener, KeyEvent.VK_R,
        KeyMapper.keyByOS(KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK)
      )
    );
    undo.addSeparator();
    mu.add(
      undo
      ,
      undoToBeginning=mu.doMenuItem(
        "Undo to beginning", undoItemListener, KeyEvent.VK_D,
        KeyMapper.key(KeyEvent.VK_F9, KeyEvent.CTRL_DOWN_MASK, KeyEvent.SHIFT_DOWN_MASK)
      )
      ,
      undoRedoToEnd=mu.doMenuItem(
        "Redo to end", undoItemListener, KeyEvent.VK_T,
        KeyMapper.key(KeyEvent.VK_F12, KeyEvent.CTRL_DOWN_MASK, KeyEvent.SHIFT_DOWN_MASK)
      )
      ,
      undoToHistorySwitch=mu.doMenuItem(
        "Undo to history switch", undoItemListener, KeyEvent.VK_H
      )
      ,
      redoToHistorySwitch=mu.doMenuItem(
        "Redo to history switch", undoItemListener, KeyEvent.VK_I
      )
    );
    undo.addSeparator();
    undo.add(
      undoFast=mu.doMenuItemCheckbox(
        "Use fast undos", undoItemListener, KeyEvent.VK_F, false
      )
    );
    undo.addSeparator();
    mu.add(
      undo
      ,
      undoClearUndos=mu.doMenuItem("Clear undo stack", undoItemListener)
      ,
      undoClearRedos=mu.doMenuItem("Clear redo stack", undoItemListener)
      ,
      undoClearBoth=mu.doMenuItem("Clear both", undoItemListener)
    );


    //SELECTION:
    mu.add(
      select=mu.doMenu(bar, "Selection",   selectListener, KeyEvent.VK_E)
      ,selectUpperCase=mu.doMenuItem(
        "Uppercase selection",
        selectionItemListener, KeyEvent.VK_U
      )
      ,selectLowerCase=mu.doMenuItem(
        "Lowercase selection",
        selectionItemListener, KeyEvent.VK_L
      )
      ,selectSortLines=mu.doMenuItem(
        "Sort selected lines",
        selectionItemListener, KeyEvent.VK_S
      )
      ,selectGetSelectionSize=mu.doMenuItem(
        "Get selection size",
        selectionItemListener, KeyEvent.VK_G
      )
      ,selectGetAsciiValues=mu.doMenuItem(
        "Get ASCII/Unicode value(s) of selection",
        selectionItemListener, KeyEvent.VK_A
      )
    );
    if (extraPopups) {
      pSelect=makePopup(select, "Selection:");
      pSelect.addPopupMenuListener(popupSelectListener);
    }

    //ALIGN:
    mu.add(
      align=mu.doMenu(bar, "Align", KeyEvent.VK_A)
      ,
      weirdInsertToAlign=mu.doMenuItem(
        "Insert spaces to align cursor to above"+(
          currentOS.isOSX
            ?"  ( ^ space)"
            :""
        ),
        alignItemListener, KeyEvent.VK_I,
        KeyMapper.key(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK)
      )
      ,
      weirdInsertToAlignBelow=mu.doMenuItem(
        "Insert spaces to align cursor to below"+(
          currentOS.isOSX
            ?"  (^ ⇧ space)"
            :""
        ),
        alignItemListener, KeyEvent.VK_N,
        KeyMapper.key(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK, KeyEvent.SHIFT_DOWN_MASK)
      )
      ,
      weirdBackspaceToAlign=mu.doMenuItem(
        "Backspace to align cursor to above",
        alignItemListener, KeyEvent.VK_B,
        KeyMapper.key(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK)
      )
      ,
      weirdBackspaceToAlignBelow=mu.doMenuItem(
        "Backspace to align cursor to below",
        alignItemListener, KeyEvent.VK_C,
        KeyMapper.key(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK, KeyEvent.SHIFT_DOWN_MASK)
      )
      ,
      weirdAlignOneRight=mu.doMenuItem(
        "One space to the right",
        alignItemListener, KeyEvent.VK_R,
        KeyMapper.key(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK)
      )
      ,
      weirdAlignOneLeft=mu.doMenuItem(
        "One space to the left",
        alignItemListener, KeyEvent.VK_L,
        KeyMapper.key(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK, KeyEvent.SHIFT_DOWN_MASK)
      )
    );

    //EXTERNAL:
    mu.add(
      external=mu.doMenu(bar, "External", KeyEvent.VK_X)
      ,externalRunBatch=mu.doMenuItem(
        "Run batch program",
        externalItemListener, KeyEvent.VK_R,
        KeyMapper.keyByOS(KeyEvent.VK_E)
      )
    );

    //OSX SHORTCUTS:
    if (extraPopups){
      mu.add(
        osxShortcuts=mu.doMenu(bar, "MacOS Shortcuts", 0)
        ,osxOpenFrom=mu.doMenuItem(
          "Open from...", osxShortcutListener, 0,
          KeyMapper.key(KeyEvent.VK_O, KeyMapper.shortcutByOS(), KeyEvent.SHIFT_DOWN_MASK)
        )
        ,osxSaveTo=mu.doMenuItem(
          "Save to...", osxShortcutListener, 0,
          KeyMapper.key(KeyEvent.VK_T, KeyMapper.shortcutByOS(), KeyEvent.SHIFT_DOWN_MASK)
        )
        ,osxFavorite=mu.doMenuItem(
          "Favorite files...", osxShortcutListener, 0,
          KeyMapper.key(KeyEvent.VK_I, KeyMapper.shortcutByOS(), KeyEvent.SHIFT_DOWN_MASK)
        )
        ,osxReopen=mu.doMenuItem(
          "Reopen file...", osxShortcutListener, 0,
          KeyMapper.key(KeyEvent.VK_R, KeyMapper.shortcutByOS(), KeyEvent.SHIFT_DOWN_MASK)
        )
      );
      osxShortcuts.addSeparator();
      mu.add(
        osxShortcuts
        ,osxSwitch=mu.doMenuItem(
          "Switch menu...", osxShortcutListener, 0,
          KeyMapper.key(KeyEvent.VK_I, KeyMapper.shortcutByOS())
        )
        ,osxSelect=mu.doMenuItem(
          "Selection menu...", osxShortcutListener, 0,
          KeyMapper.key(KeyEvent.VK_L, KeyMapper.shortcutByOS())
        )
      );
    }


    //OPTIONS:
    mu.add(
      options=mu.doMenu(bar, "Options",   KeyEvent.VK_O)
      ,optionWordWrap=mu.doMenuItemCheckbox(
        "Word wrap", optionListener, KeyEvent.VK_W, false
      )
      ,optionAutoTrim=mu.doMenuItemCheckbox(
        "Auto-trim trailing spaces on save", optionListener, KeyEvent.VK_A, false
      )
      ,
      optionFontBigger=mu.doMenuItem(
        "Make font bigger", optionListener, KeyEvent.VK_B,
        KeyMapper.key(KeyEvent.VK_EQUALS, KeyMapper.shortcutByOS())
      )
      ,
      optionFontSmaller=mu.doMenuItem(
        "Make font smaller", optionListener, KeyEvent.VK_B,
        KeyMapper.key(KeyEvent.VK_MINUS, KeyMapper.shortcutByOS())
      )
    );
    options.addSeparator();
    mu.add(
      options
      ,optionTabsAndIndents=mu.doMenuItem("Tabs & Indents",               optionListener, KeyEvent.VK_T)
      ,optionLineDelimiters=mu.doMenuItem("Line delimiters",              optionListener, KeyEvent.VK_L)
      ,optionFont          =mu.doMenuItem("Font & colors",                optionListener, KeyEvent.VK_F)
      ,optionFavorites     =mu.doMenuItem("Favorite files & directories", optionListener, KeyEvent.VK_V)
      ,optionSSH           =mu.doMenuItem("SSH Options",                  optionListener, KeyEvent.VK_S)
    );

    //HELP:
    mu.add(
      help=mu.doMenu(bar, "Help",   KeyEvent.VK_H)
      ,helpAbout   =mu.doMenuItem(
        "About Klonk",                   helpListener, KeyEvent.VK_A
      )
      ,helpShortcut=mu.doMenuItem(
        "Shortcuts and hidden features", helpListener, KeyEvent.VK_S, KeyMapper.key(KeyEvent.VK_F1)
      )
    );
  }

  ///////////////////////////////////////
  // POPUP MENU CREATION FOR OSX ONLY: //
  ///////////////////////////////////////

  private JPopupMenu makePopup(JMenu from, String label) {
    JPopupMenu menu=from.getPopupMenu();
    labelPopup(menu, label);
    return menu;
  }
  private JPopupMenu makeLabelledPopup(String label) {
    JPopupMenu menu=new JPopupMenu();
    labelPopup(menu, label);
    return menu;
  }
  private void labelPopup(JPopupMenu menu, String label) {
    JLabel lbl=new javax.swing.JLabel(label);
    lbl.setFont(file.getFont());
    lbl.setBorder(
      new javax.swing.border.EmptyBorder(
        new java.awt.Insets(2,10,2,2)
      )
    );
    menu.insert(lbl, 0);
    menu.insert(new JSeparator(javax.swing.SwingConstants.HORIZONTAL), 1);
  }

  //////////////////////
  // EVENT LISTENERS: //
  //////////////////////

  // Listener maps map menu items to runnable listeners. Not used
  // for everything, but where convenient:
  private static class ListenerMap {
    private Map<Object, Runnable> map=new HashMap<>();
    public ListenerMap add(Object x, Runnable r) {
      map.put(x, r);
      return this;
    }
    public void run(Object x) {
      Runnable r=map.get(x);
      if (r==null) throw new RuntimeException("No mapping for menu: "+x);
      r.run();
    }
  }
  private final ListenerMap
    fileListenerMap=new ListenerMap(),
    markListenerMap=new ListenerMap(),
    searchListenerMap=new ListenerMap(),
    undoListenerMap=new ListenerMap(),
    optionListenerMap=new ListenerMap(),
    selectionListenerMap=new ListenerMap(),
    alignListenerMap=new ListenerMap();

  private void listen() {
    fileListenerMap
      .add(fileOpen,         ()->ctrlMain.doFileOpenDialog())
      .add(fileNew,          ()->ctrlMain.doNew())
      .add(fileSave,         ()->ctrlMain.doSave())
      .add(fileSaveAs,       ()->ctrlMain.doSave(true))
      .add(fileClose,        ()->ctrlMain.doFileClose())
      .add(fileCloseOthers,  ()->ctrlMain.doFileCloseOthers())
      .add(fileCloseAll,     ()->ctrlMain.doFileCloseAll())
      .add(filePrint,            ()->ctrlFileOther.doPrint())
      .add(fileEncrypt,          ()->ctrlMain.doEncrypt())
      .add(fileDocDirExplore,    ()->ctrlFileOther.doDocumentDirectoryExplore())
      .add(fileClipboardDoc,     ()->ctrlFileOther.doClipboardDoc())
      .add(fileClipboardDocDir,  ()->ctrlFileOther.doClipboardDocDir())
      .add(fileFind,             ()->ctrlMain.doFileFind())
      .add(fileOpenFromDocDir,   ()->ctrlMain.doOpenFromDocDir())
      .add(fileOpenFromList,     ()->ctrlMain.doOpenFromList())
      .add(fileOpenFromSSH,      ()->ctrlMain.doOpenFromSSH())
      .add(fileSaveToDocDir,     ()->ctrlMain.doSaveToDocDir())
      .add(fileSaveToSSH,        ()->ctrlMain.doSaveToSSH())
      .add(fileFaveAddDir,       ()->ctrlFileOther.doAddCurrentToFaveDirs())
      .add(fileFaveAddFile,      ()->ctrlFileOther.doAddCurrentToFaveFiles())
      .add(fileExit,             ()->ctrlMain.doFileExit())
      ;
    searchListenerMap
      .add(searchRepeat,          ()->ctrlSearch.doSearchRepeat())
      .add(searchFind,            ()->ctrlSearch.doSearchFind())
      .add(searchReplace,         ()->ctrlSearch.doSearchReplace())
      .add(searchRepeatBackwards, ()->ctrlSearch.doSearchRepeatBackwards())
      .add(searchGoToLine,        ()->ctrlSearch.doSearchGoToLine())
      ;
    markListenerMap
      .add(markSet,          ()->ctrlMarks.doMarkSet())
      .add(markGoToPrevious, ()->ctrlMarks.doMarkGoToPrevious())
      .add(markGoToNext,     ()->ctrlMarks.doMarkGoToNext())
      .add(markClearCurrent, ()->ctrlMarks.doMarkClearCurrent())
      .add(markClearAll,     ()->ctrlMarks.doMarkClearAll())
      ;
    undo.addMenuListener(new MenuListener(){
      public void menuCanceled(MenuEvent e){}
      public void menuDeselected(MenuEvent e){}
      public void menuSelected(MenuEvent e){
        enableDisableUndoRedo();
      }
    });
    undoListenerMap
      .add(undoUndo,        ()->ctrlUndo.doUndo())
      .add(undoRedo,        ()->ctrlUndo.doRedo())
      .add(undoFast,        ()->ctrlUndo.doUndoFast())
      .add(undoToBeginning, ()->ctrlUndo.doUndoToBeginning())
      .add(undoRedoToEnd,   ()->ctrlUndo.doRedoToEnd())
      .add(undoToHistorySwitch, ()->ctrlUndo.undoToHistorySwitch())
      .add(redoToHistorySwitch, ()->ctrlUndo.redoToHistorySwitch())
      .add(undoClearUndos,  ()->ctrlUndo.doClearUndos())
      .add(undoClearRedos,  ()->ctrlUndo.doClearRedos())
      .add(undoClearBoth,   ()->ctrlUndo.doClearUndosAndRedos())
      ;
    optionListenerMap
      .add(optionWordWrap,        ()->ctrlOptions.doWordWrap())
      .add(optionAutoTrim,        ()->ctrlOptions.doAutoTrim())
      .add(optionTabsAndIndents,  ()->ctrlOptions.doTabsAndIndents())
      .add(optionLineDelimiters,  ()->ctrlOptions.doLineDelimiters())
      .add(optionFont,            ()->ctrlOptions.doFontAndColors())
      .add(optionFontBigger,      ()->ctrlOptions.doFontBigger())
      .add(optionFontSmaller,     ()->ctrlOptions.doFontSmaller())
      .add(optionFavorites,       ()->ctrlOptions.doFavorites())
      .add(optionSSH,             ()->ctrlOptions.doSSH())
      ;
    selectionListenerMap
      .add(selectUpperCase,             ()->ctrlSelection.doWeirdUpperCase())
      .add(selectLowerCase,             ()->ctrlSelection.doWeirdLowerCase())
      .add(selectSortLines,             ()->ctrlSelection.doWeirdSortLines())
      .add(selectGetSelectionSize,      ()->ctrlSelection.doWeirdSelectionSize())
      .add(selectGetAsciiValues,        ()->ctrlSelection.doWeirdAsciiValues())
      ;
    alignListenerMap
      .add(weirdInsertToAlign,         ()->editors.getFirst().doInsertToAlign(true))
      .add(weirdInsertToAlignBelow,    ()->editors.getFirst().doInsertToAlign(false))
      .add(weirdBackspaceToAlign,      ()->editors.getFirst().doBackspaceToAlign(true))
      .add(weirdBackspaceToAlignBelow, ()->editors.getFirst().doBackspaceToAlign(false))
      .add(weirdAlignOneRight,         ()->editors.getFirst().moveRightOnce())
      .add(weirdAlignOneLeft,          ()->editors.getFirst().moveLeftOnce())
      ;
  }
  private ActionListener
    fileListener=(ActionEvent event) -> fileListenerMap.run(event.getSource())
    ,
    //These have to be individualized because they require the text of the
    //item that fired them, can't detect which.
    reopenListener=(ActionEvent event)-> {
      JMenuItem s=(JMenuItem) event.getSource();
      ctrlMain.doLoadFile(s.getText());
    }
    ,
    openFromListener=(ActionEvent event)-> {
      JMenuItem s=(JMenuItem) event.getSource();
      ctrlMain.doOpenFrom(s.getText());
    }
    ,
    saveToListener=(ActionEvent event)-> {
      JMenuItem s=(JMenuItem) event.getSource();
      ctrlMain.doSaveTo(s.getText());
    }
    ,

    searchListener=(ActionEvent event)->searchListenerMap.run(event.getSource())
    ,
    markItemListener=
      // Note that CtrlMarks will actually calls us back via our
      // "markStateListener" instance to let us know when to disable/enable
      // the marks menu items. (We also have a MenuListener further down
      // that actually catches the user selecting the marks menu and updates the
      // contents, but that doesn't enable/disable.)
      (ActionEvent event) -> markListenerMap.run(event.getSource())
    ,
    switchListener=(ActionEvent event) -> {
      JMenuItem s=(JMenuItem) event.getSource();
      //Two things can happen, send go back or pick a file:
      if (s==switchBackToFront)
        ctrlMain.doSendBackToFront();
      else
      if (s==switchFrontToBack)
        ctrlMain.doSendFrontToBack();
      else
      if (s==switchNextUnsaved || s==pSwitchNextUnsaved)
        ctrlMain.doSwitchToNextUnsaved();
      else {
        Editor e=switchMenuToEditor.get(s);
        if (e==null)
          throw new RuntimeException("Menus.switchListener(): Null editor in hash");
        ctrlMain.doSwitch(e);
      }
    }
    ,
    undoItemListener=(ActionEvent event)-> undoListenerMap.run(event.getSource())
    ,
    selectionItemListener=(ActionEvent event)->selectionListenerMap.run(event.getSource())
    ,
    alignItemListener=(ActionEvent event)->alignListenerMap.run(event.getSource())
    ,
    externalItemListener=(ActionEvent event)-> {
      Object s=event.getSource();
      if (s==externalRunBatch)        ctrlOther.doShell();
      else
        throw new RuntimeException("Unexpected "+s);
    }
    ,
    optionListener=(ActionEvent event)->optionListenerMap.run(event.getSource())
    ,
    osxShortcutListener=(ActionEvent event)-> {
      Object o=event.getSource();
      JPopupMenu jpm;
      if (o==osxOpenFrom) jpm=pOpenFrom;
      else
      if (o==osxSaveTo)   jpm=pSaveTo;
      else
      if (o==osxFavorite) jpm=pFavorite;
      else
      if (o==osxReopen)   jpm=pReopen;
      else
      if (o==osxSelect)   jpm=pSelect;
      else
      if (o==osxSwitch)   jpm=pswitcher;
      else
        throw new RuntimeException("No popup menu matched: "+o);
      jpm.show(osxAttachPopupsTo, 0, 0);
    }
    ,
    helpListener=(ActionEvent event)-> {
      Object s=event.getSource();
      if (s==helpShortcut)
        ctrlOther.doHelpShortcuts();
      else
      if (s==helpAbout)
        ctrlOther.doHelpAbout();
    }
    ;

  //////////////////////////////////
  // REAL-TIME ON-SHOW LISTENERS: //
  //////////////////////////////////
  private void enableDisableUndoRedo() {
    final boolean
      hasRedos=editors.getFirst().hasRedos(),
      hasUndos=editors.getFirst().hasUndos();
    undoUndo.setEnabled(hasUndos);
    undoRedo.setEnabled(hasRedos);
    //Without an actual listener watching undo/redo stacks this is just
    //too hard to do.
    //undoToBeginning.setEnabled(hasUndos);
    //undoRedoToEnd.setEnabled(hasRedos);
    undoToHistorySwitch.setEnabled(hasUndos);
    redoToHistorySwitch.setEnabled(hasRedos);
    undoClearUndos.setEnabled(hasUndos);
    undoClearRedos.setEnabled(hasRedos);
    undoClearBoth.setEnabled(hasRedos || hasUndos);
  }
  private void enableSelectItems() {
    boolean selected=editors.getFirst().isAnythingSelected();
    selectUpperCase.setEnabled(selected);
    selectLowerCase.setEnabled(selected);
    selectSortLines.setEnabled(selected);
    selectGetSelectionSize.setEnabled(selected);
    selectGetAsciiValues.setEnabled(selected);
  }
  private PopupMenuListener
    popupSelectListener=new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent e){}
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
      public void popupMenuWillBecomeVisible(PopupMenuEvent e){
        enableSelectItems();
      }
    };
  private MenuListener
    selectListener=new MenuListener(){
      public void menuCanceled(MenuEvent e){}
      public void menuDeselected(MenuEvent e){}
      public void menuSelected(MenuEvent e){
        enableSelectItems();
      }
    }
    ,
    markListener=new MenuListener() {
      public void menuCanceled(MenuEvent e){}
      public void menuDeselected(MenuEvent e){}
      public void menuSelected(MenuEvent e){
        int prev=editors.getFirst().getPreviousMark(),
            count=editors.getFirst().getMarkCount();
        if (count==0){
          markGoToPrevious.setText("Go to previous mark");
          markGoToNext.setText("Go to next mark");
        }
        else {
          markGoToPrevious.setText(
            "Go to previous mark - "+(prev==-1 ?"None" :prev+" of "+count)
          );
          markGoToNext.setText(
            "Go to next mark - "+(prev==count ?"None" :(prev+1)+" of "+count)
          );
        }
      }
    }
  ;

  /**
   * We hand this to editors and ask them to call it whenever keystrokes happen. This is how
   * we get "alternate" / hidden keys to work, or really to enable Windows key combinations
   * to work on OSX as well.
   */
  private void listenToEditorKeys(KeyEvent e) {
    if (currentOS.isOSX) {
      //This is all so we can get MS Windows capabilities on OSX
      //even though the corresponding menu items are attached to other keystrokes.
      final int code=e.getKeyCode();
      final int modifiers=e.getModifiersEx();

      // Marks:
      if (code==e.VK_F4) {
        if (KeyMapper.shiftPressed(modifiers))
          ctrlMarks.doMarkClearCurrent();
        else
          ctrlMarks.doMarkSet();
        e.consume();
      }
      else
      if (code==e.VK_F8 && modifiers==0) {
        ctrlMarks.doMarkGoToPrevious();
        e.consume();
      }
      else
      if (code==e.VK_F9 && modifiers==0) {
        ctrlMarks.doMarkGoToNext();
        e.consume();
      }
      else

      //Find:
      if (code==e.VK_F3) {
        e.consume();
        if (KeyMapper.shiftPressed(modifiers))
          ctrlSearch.doSearchRepeatBackwards();
        else
          ctrlSearch.doSearchRepeat();
      }
      else
      if (code==e.VK_F && KeyMapper.ctrlPressed(modifiers)){
        e.consume();
        ctrlSearch.doSearchFind();
      }
      else
      if (code==e.VK_R && KeyMapper.ctrlPressed(modifiers)) {
        e.consume();
        ctrlSearch.doSearchReplace();
      }
      else
      if (code==e.VK_G && KeyMapper.ctrlPressed(modifiers)) {
        e.consume();
        ctrlSearch.doSearchGoToLine();
      }

      //Save:
      else
      if (code==e.VK_S && KeyMapper.ctrlPressed(modifiers)) {
        e.consume();
        ctrlMain.doSave();
      }

      //Switch:
      else
      if (code==e.VK_F11) {
        e.consume();
        if (KeyMapper.shiftPressed(modifiers))
          ctrlMain.doSendBackToFront();
        else
          ctrlMain.doSendFrontToBack();
      }
    }
  }
}
