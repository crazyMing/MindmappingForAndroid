/*
 This file is part of MindMap.

    MindMap is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    MindMap is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MindMap; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

package edu.agh.klaukold.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.agh.R;
import edu.agh.klaukold.commands.EditBox;
import edu.agh.klaukold.commands.EditSheet;
import edu.agh.klaukold.common.Box;
import edu.agh.klaukold.common.Root;
import edu.agh.klaukold.common.Sheet;
import edu.agh.klaukold.common.Text;
import edu.agh.klaukold.enums.Actions;
import edu.agh.klaukold.enums.Align;
import edu.agh.klaukold.enums.BlockShape;
import edu.agh.klaukold.enums.LineStyle;
import edu.agh.klaukold.enums.LineThickness;
import edu.agh.klaukold.interfaces.Command;
import edu.agh.klaukold.utilities.AsyncInvalidate;
import edu.agh.klaukold.utilities.Callback;
import edu.agh.klaukold.utilities.DialogFactory;
import edu.agh.klaukold.utilities.Utils;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private GestureDetector gestureDetector;
    public static DrawView lay;
    public static ActionMode mActionMode;
    private MoveBoxCallback moveCallback = new MoveBoxCallback();
    private DeleteBoxCallback callback = new DeleteBoxCallback();
    private boolean mIsScrolling = false;
    private GestureListener gestList = new GestureListener();
    public static Root root;
    public static Sheet sheet = new Sheet();
    public static Box boxEdited;

    public static LinkedList<Command> commandsUndo = new LinkedList<Command>();
    public static LinkedList<Command> commandsRedo = new LinkedList<Command>();
    private static Menu menu;
    private static Resources res;


    public static int id = 1;

    private PointF mid = new PointF();
    private ScaleGestureDetector detector;

    public final static String BACKGROUNDCOLOR = "COLOR";
    public final static String WALLPAPER = "WALLPAPER";
    public final static String INTENSIVITY = "INTENSIVITY";

    private String style;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lay = (DrawView) findViewById(R.id.myLay);
        res = getResources();
        if (root == null) {
            root = new Root();
            Intent intent = getIntent();
            style = intent.getStringExtra(WelcomeScreen.STYLE);
            Resources res = getResources();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x / 3;
            int height = size.y / 3;
            root.setPoint(new edu.agh.klaukold.common.Point(width, height));
            if (style.equals("Default")) {
                Text text = new Text();
                text.setAlign(Align.CENTER);
                text.setColor(new ColorDrawable(Color.BLACK));
                text.setSize(13);
                root.setShape(BlockShape.ROUNDED_RECTANGLE);
                int color = res.getColor(R.color.blue);
                root.setColor(new ColorDrawable(color));
                root.setText(text);
                root.setDrawableShape((GradientDrawable) res.getDrawable(R.drawable.round_rect));
                root.setLineStyle(LineStyle.STRAIGHT);
                root.setLineColor(Color.rgb(128, 128, 128));
                root.setLineThickness(LineThickness.THINNEST);
                sheet.setColor(new ColorDrawable(Color.WHITE));
                sheet.setIntensivity(0);
                // root.setDrawableShape((RotateDrawable)res.getDrawable(R.drawable.diammond));
                //RorateDrawable dla diamond
                // root.setDrawableShape((GradientDrawable)res.getDrawable(R.drawable.rect));
                //TODO dopisac cechy stylu
            }
            //TODO dopisa style
        }
        //root.draw();
        gestureDetector = new GestureDetector(this, gestList);
//Object r =  findViewById(R.id.action_settings);
//        View.OnClickListener onClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, EditSheetScreen.class);
//                intent.putExtra(SHEET, sheet);
//                startActivity(intent);
//            }
//        };
        // settings.setOnClickListener(onClickListener);
        Utils.lay = lay;
        Utils.context = this;
//	    //zeby byla czysta mapa przy wczytywaniu nowej
//	    core = null;
//	    id = 1;
//	    
//	    if(getIntent() != null && getIntent().getStringExtra("filename") != null) {
//	    	//DbAdapter.filename = getIntent().getStringExtra("filename");
//	    }
//	    
        if (getIntent() != null && getIntent().getBooleanExtra("present", false)) {
            Callback call = new Callback() {
                @Override
                public void execute() {
                    //core = Utils.db.getCore();

                    for (Box box : root.getLeftChildren()) {
                        //	lay.addLine(core, box);
                        Utils.drawAllLines(box);
                    }

                    for (Box box : root.getRightChildren()) {
                        //lay.addLine(core, box);
                        Utils.drawAllLines(box);
                    }

//					for(Box box: core.detached) {
//						Utils.drawAllLines(box);
//					}
                }
            };
//			
            AsyncInvalidate async = new AsyncInvalidate(this);
            async.setCallback(call);
            async.execute();
        }
//	    } else if(getIntent() != null && getIntent().getBooleanExtra("import", false)) { 
//	    	Utils.loadMaps(this);
//	    }
// else {
//	    	core = new Core();
//	    	core.setText("CENTRAL BOX");
//	    	core.setId((id++)+"");
//	    	core.rect = new Rect(200, 120, 300, 220);
//		   // Utils.db.insertCore(core);
//		    lay.revalidate();
//		    core.refresh();
//		    lay.invalidate();
//	    }
//	    
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//		
        lay.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case (MotionEvent.ACTION_OUTSIDE):
                        return true;

                    case (MotionEvent.ACTION_UP):
                        gestList.click = false;
                        if (mIsScrolling) {
                            String txt = "default text";
                            Text t = new Text();
                            t.setText(txt);
                            gestList.myRect.setText(t);

                            gestList.myRect.setParent(gestList.clicked);
                            //gestList.myRect.setId(Utils.giveId()+"");

                            //editContent(gestList.myRect);

//						if(gestList.clicked.getId().equals(root.getId())) {
//							if(core.mid_x < gestList.myRect.rect.left) {
//								gestList.myRect.position = Position.RIGHT;
//								lay.updateRight = true;
//							} else {
//								gestList.myRect.position = Position.LEFT;
//								lay.updateLeft = true;
//							}

                            //root.addChild(gestList.myRect);
                            //	lay.addLine(gestList.clicked, gestList.myRect);
                            //	Utils.db.insertTopic(gestList.myRect);
                            //	Utils.db.updateCore(core);
                        } else {
//							if(gestList.clicked.position == Position.LEFT) {
//								gestList.myRect.position = Position.LEFT;
//								lay.updateLeft = true;
//							} else {
//								gestList.myRect.position = Position.RIGHT;
//								lay.updateRight = true;
                        }

                        //	gestList.clicked.addChild(gestList.myRect);
                        //	lay.addLine(gestList.clicked, gestList.myRect);
                        //Utils.db.insertTopic(gestList.myRect);
                        //Utils.db.updateTopic(gestList.clicked);
                        lay.revalidate();
                        lay.invalidate();

                        mIsScrolling = false;

                        gestList.clicked = null;
                        gestList.myRect = new Box();

                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // multitouch!! - touch down
                        int count = event.getPointerCount(); // Number of 'fingers' in this time

                        if (count > 1) {
                            Box b1 = Utils.whichBox(lay, event, 0);
                            Box b2 = Utils.whichBox(lay, event, 1);


                            mIsScrolling = false;
                            return true;
                        } else {
                            return detector.onTouchEvent(event);
                        }
                    case MotionEvent.ACTION_POINTER_UP:
                        if (event.getPointerCount() > 1) {
                            return detector.onTouchEvent(event);
                        }

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() > 1) {
                            return detector.onTouchEvent(event);
                        }

                    default:
                        break;
                }

                boolean response = gestureDetector.onTouchEvent(event);
                lay.requestFocus();
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(lay.getApplicationWindowToken(), 0);

                return response;
            }
        });
//		
        //menu domyślne
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.show();
//
        detector = new ScaleGestureDetector(this, new SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                lay.setPivotX(mid.x);
                lay.setPivotY(mid.y);
                lay.zoomx *= detector.getScaleFactor();
                lay.zoomy *= detector.getScaleFactor();
                lay.revalidate();
                lay.invalidate();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        lay.setBackgroundColor(sheet.getColor().getColor());
    }

    //tutaj rozpoznajemy przytrzymanie, jedno kliknięcie, dwa kliknięcia
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        Box myRect = new Box();
        Box clicked;
        boolean click = false;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Pair<Box, Actions> pair = Utils.whichBoxAction(lay, event);
            Box box = Utils.whichBox(lay, event);
            if (box != null) {
                box.setSelected(true);
                MainActivity.boxEdited = box;
                menu.getItem(1).setVisible(true);
                menu.getItem(2).setVisible(true);
                lay.invalidate();
            } else if (box == null) {
                root.setSelected(false);
                for (int i = 0; i < root.getLeftChildren().size(); i++) {
                    root.getLeftChildren().get(i).setSelected(false);
                }
                for (int i = 0; i < root.getRightChildren().size(); i++) {
                    root.getLeftChildren().get(i).setSelected(false);
                }
                menu.getItem(1).setVisible(false);
                menu.getItem(2).setVisible(false);
                lay.invalidate();
            }
            if (pair != null) {

                if (pair.second == Actions.EDIT_BOX) {
                    boxEdited = pair.first;
                    Intent intent = new Intent(MainActivity.this, EditBoxScreen.class);
                    intent.putExtra(EditBoxScreen.BOX_COLOR, pair.first.getColor().getColor());
                    intent.putExtra(EditBoxScreen.TEXT_COLOR, pair.first.getText().getColor().getColor());
                    intent.putExtra(EditBoxScreen.LINE_SHAPE, pair.first.getShape());
                    intent.putExtra(EditBoxScreen.LINE_COLOR, pair.first.getLineColor());
                    intent.putExtra(EditBoxScreen.LINE_SHAPE, pair.first.getLineStyle());
                    intent.putExtra(EditBoxScreen.BOX_SHAPE, pair.first.getShape());
                    intent.putExtra(EditBoxScreen.LINE_THICKNESS, pair.first.getLineThickness());
                    startActivity(intent);

                } else if (pair.second == Actions.NEW_NOTE) {

                } else if (pair.second == Actions.NEW_MARKER) {

                }
            }
//            if(mActionMode == null && clicked != null) {
//            	if(clicked.isVisible()) {
//            		//clicked.changeDescendantsVisibility();
//            	}
//            	clicked = null;
//            	lay.revalidate();
//            	lay.invalidate();
//            }
//            
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!click || Utils.whichBox(lay, e) == null) {
                return;
            }

            if (mActionMode == null) {
                mActionMode = startActionMode(callback);
                mActionMode.setTitle("Move");
            }

            if (mActionMode.getTitle().toString().equalsIgnoreCase("move")) {
                boolean b = clicked.isSelected();
                if (b) {
                    callback.removeObserver(clicked);
                } else {
                    callback.addObserver(clicked);
                }

                lay.invalidate();
            }

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mActionMode != null) {
                if (clicked != null && click) {
                    int newx = (int) (e2.getX() - e1.getX());
                    int newy = (int) (e2.getY() - e1.getY());

                    newx /= lay.zoomx;
                    newy /= lay.zoomy;

                    newx = (int) -distanceX;
                    newy = (int) -distanceY;

                    Utils.moveChildX(clicked, newx);
                    Utils.moveChildY(clicked, newy);
                    lay.revalidate();
                    lay.invalidate();
                    return true;
                }
            } else if (click && clicked != null) {
                mIsScrolling = true;
                int newx = (int) (e2.getX() - lay.transx);
                int newy = (int) (e2.getY() - lay.transy);

                newx /= lay.zoomx;
                newy /= lay.zoomy;

                //Rect r = new Rect(newx, newy, newx + 100, newy + 50);

                //myRect.rect.set();
                return false;
            }

            lay.transx -= distanceX;
            lay.transy -= distanceY;
            lay.invalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (mActionMode != null && mActionMode.getTitle().toString().equalsIgnoreCase("move")) {
                if (Utils.whichBox(lay, e) == clicked) {
                    click = true;
                }
            } else {
                clicked = Utils.whichBox(lay, e);
                if (clicked != null) {
                    click = true;
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mActionMode != null && mActionMode.getTitle().toString().equalsIgnoreCase("move")) {
                return true;
            }

            if (Utils.whichBox(lay, e) != null) {
                editContent(Utils.whichBox(lay, e));
                return true;
            }
//
//	        final Dialog dialog = DialogFactory.boxContentDialog(MainActivity.this);
//	        final EditText et = (EditText) dialog.findViewById(R.id.editText);
//	        et.requestFocus();

            //final Box myClicked = new Box();
            // myClicked.create(Utils.getCoordsInView(lay, e, 0));
            // myClicked.getText().setText("default text");

//	        if(root.getMidX() < (myClicked.getDrawableShape().getBounds().left + myClicked.getDrawableShape().getBounds().right)/2) {
//	        //	myClicked.position = Position.RIGHT;
//				lay.updateRight = true;
//			} else {
//			//	myClicked.position = Position.LEFT;
//				lay.updateLeft = true;
//			}

            // myClicked.setId(Utils.giveId()+"");
            // MainActivity.root.addChild(myClicked);

            //myClicked.setTimestamp(new Date().getTime());

            // lay.triggerLastDetachMove();
            // lay.revalidate();

//	        try {
//	        	Callback call = new Callback() {
//					@Override
//					public void execute() {
//						//.db.insertTopic(myClicked);
//				        //Utils.db.updateCore(core);
//					}
//				};
//
//				AsyncInvalidate async = new AsyncInvalidate(MainActivity.this);
//				async.setCallback(call);
//				async.execute();
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}

            // editContent(myClicked);
            return true;
        }
    }

    private class DeleteBoxCallback implements ActionMode.Callback {
        List<Box> observers = new ArrayList<Box>();

        public void addObserver(Box box) {
            if (!observers.contains(box)) {
                box.setSelected(true);
                observers.add(box);
            }
        }

        public void removeObserver(Box box) {
            observers.remove(box);
            box.setSelected(false);
            if (observers.isEmpty()) {
                mActionMode.finish();
            }
        }

        private void notifyObservers() {
            for (Box box : observers) {
                box.setSelected(false);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//            	case R.id.menu_delete:
//
//            	for(Box v: observers) {
//            		if(v instanceof Core) {
//            			continue;
//            		}
//
//            		//lay.deleteHimAndChildren(v);
//            		//Utils.db.deleteChild(v.getId());
//            	}
//
//                mode.finish(); // Action picked, so close the CAB
//                return true;
//            default:
//                return false;
//            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
//            // remove selection
//        	notifyObservers();
//        	observers.clear();
//
//        	lay.revalidate();
//        	lay.invalidate();
//        	mActionMode = null;
        }
    }

    private class MoveBoxCallback implements ActionMode.Callback {
        Box observer;

        public void setObserver(Box box) {
            if (observer == null && !(box instanceof Root)) {
                observer = box;
                observer.setSelected(true);
            }
        }

        public void removeObserver() {
            mActionMode.finish();
        }

        private void notifyObserver() {
            if (observer != null) {
                observer.setSelected(false);
                determinePosition();
            }
        }

        private void determinePosition() {
            if (observer.getParent() instanceof Root) {
                Root core = (Root) observer.getParent();

                if (observer.getPoint()!= null) {
                    observer.getPoint().x = observer.getDrawableShape().getBounds().left;
                    observer.getPoint().y = observer.getDrawableShape().getBounds().top;
                    if ((observer.getDrawableShape().getBounds().left + observer.getDrawableShape().getBounds().right) / 2 < core.getPoint().x/2) {
                      //  observer.position = Position.LEFT;
                        ;
                    } else {
                       // observer.position = Position.RIGHT;
                    }
                    Utils.propagatePosition(observer, observer.getPoint());
                    return;
                }

                core.getRightChildren().remove(observer);
                core.getLeftChildren().remove(observer);

                if ((core.getPoint().x / 2) < observer.getDrawableShape().getBounds().left) {
                   // Utils.propagatePosition(observer, );
                    lay.updateRight = true;

                    int ind = core.getRightChildren().size();

                    for (int i = 0; i < core.getRightChildren().size(); i++) {
                        if (core.getRightChildren().get(i).getDrawableShape().getBounds().top > observer.getDrawableShape().getBounds().top) {
                            ind = i;
                            break;
                        }
                    }

                    core.getRightChildren().add(ind, observer);
                } else {
                    //Utils.propagatePosition(observer, Position.LEFT);
                    lay.updateLeft = true;

                    int ind = core.getLeftChildren().size();

                    for (int i = 0; i < core.getLeftChildren().size(); i++) {
                        if (core.getLeftChildren().get(i).getDrawableShape().getBounds().top > observer.getDrawableShape().getBounds().top) {
                            ind = i;
                            break;
                        }
                    }

                    core.getLeftChildren().add(ind, observer);
                }
            } else {
                List<Box> siblings = observer.getParent().getChildren();
                siblings.remove(observer);

                int ind = siblings.size();

                for (int i = 0; i < siblings.size(); i++) {
                    if (siblings.get(i).getDrawableShape().getBounds().top > observer.getDrawableShape().getBounds().top) {
                        ind = i;
                        break;
                    }
                }

                siblings.add(ind, observer);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.menu_delete:
//
//                    mode.finish();
//                    return true;
//                default:
                   return false;
//            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            notifyObserver();
            observer = null;

            lay.revalidate();
            lay.invalidate();

            mActionMode = null;
        }
    }

    private void editContent(final Box myClicked) {
        final Dialog dialog = DialogFactory.boxContentDialog(MainActivity.this);
        final Button btn = (Button) dialog.findViewById(R.id.dialogButtonOK);
        final EditText et = (EditText) dialog.findViewById(R.id.editText);
        et.requestFocus();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Callback call = null;

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                Text text = null;
                try {
                    text = (Text) myClicked.getText().TextClone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                text.setText(et.getText().toString());
//                Text text = myClicked.getText();
//                text.setText(et.getText().toString());
                //myClicked.getText().setText(et.getText().toString());
                EditBox editBox = new EditBox();
                Properties properties = new Properties();
                properties.put("box", myClicked);
                properties.put("box_text", text);
                editBox.execute(properties);
                addCommendUndo(editBox);
                MainActivity.menu.getItem(4).setVisible(true);
                if (myClicked instanceof Root) {
                    call = new Callback() {
                        @Override
                        public void execute() {
                            lay.updateBox(myClicked);
                        }
                    };
                } else {
                    call = new Callback() {
                        @Override
                        public void execute() {
                            lay.updateBox(myClicked);

                        }
                    };
                }

                dialog.dismiss();
                try {
                    AsyncInvalidate async = new AsyncInvalidate(MainActivity.this);
                    async.setCallback(call);
                    async.execute();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        final int MAX_LINES = 3;

        //ogranicza do 3 linii widok w zawartości bloczka
        et.addTextChangedListener(new TextWatcher() {
            private int lines;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lines = Utils.countLines(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                int counter = Utils.countLines(s.toString());

                int diff = lines - counter;
                if (diff > 0) {
                    //w gore
                    if (counter < MAX_LINES - 1 && et.getLayoutParams().height > 75) {
                        LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                        buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin - 30,
                                buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                        btn.setLayoutParams(buttonLayoutParams);
                        et.getLayoutParams().height -= 30;
                    }
                } else if (diff < 0) {
                    //w dol
                    if (counter < MAX_LINES && et.getLayoutParams().height < 135) {
                        LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                        buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin + 30,
                                buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                        btn.setLayoutParams(buttonLayoutParams);
                        et.getLayoutParams().height += 30;
                    }
                }
            }
        });

        et.setText(myClicked.getText().getText());
        int k = Utils.countLines(et.getText().toString());
        int ile = Math.min(MAX_LINES - 1, k);

        et.getLayoutParams().height = 75 + ile * 30;
        LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
        buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin,
                buttonLayoutParams.topMargin + 30 * ((k < 2) ? 0 : (k == 2) ? ile - 1 : ile),
                buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
        btn.setLayoutParams(buttonLayoutParams);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        menu.getItem(4).setVisible(false);
        menu.getItem(5).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, EditSheetScreen.class);
                intent.putExtra(BACKGROUNDCOLOR, sheet.getColor().getColor());
                intent.putExtra(INTENSIVITY, sheet.getIntensivity());
                startActivity(intent);
                //               commandsUndo.getFirst().undo();
//                lay.updateBox(root);
//                lay.revalidate();
//                lay.invalidate();
                return true;
            case R.id.action_undo:
                if (commandsUndo.size() == 1) {
                    commandsUndo.getFirst().undo();
                    if (commandsUndo.getFirst() instanceof EditBox) {
                        lay.updateBox(((EditBox) commandsUndo.getFirst()).box);
                        lay.revalidate();
                        lay.invalidate();
                    } else if (commandsUndo.getFirst() instanceof EditSheet) {
                        lay.setBackgroundColor(sheet.getColor().getColor());
                    }
                    commandsRedo.add(commandsUndo.getFirst());
                    commandsUndo.removeFirst();
                    menu.getItem(4).setVisible(false);
                    menu.getItem(5).setVisible(true);
                } else {
                    commandsUndo.getLast().undo();
                    if (commandsUndo.getLast() instanceof EditBox) {
                        lay.updateBox(((EditBox) commandsUndo.getLast()).box);
                        lay.revalidate();
                        lay.invalidate();
                    } else if (commandsUndo.getLast() instanceof EditSheet) {
                        lay.setBackgroundColor(sheet.getColor().getColor());
                    }
                    commandsRedo.add(commandsUndo.getLast());
                    commandsUndo.removeLast();
                }
                return true;
            case R.id.action_new:
                Box box = new Box();
                if (boxEdited instanceof Root) {
                    if (root.getLeftChildren().size() == root.getRightChildren().size()) {
                        root.getLeftChildren().add(box);
                    } else {
                        root.getRightChildren().add(box);
                    }
                } else {
                    boxEdited.addChild(box);
                }
                //todo tylko probne
                box.setParent(boxEdited);
                box.setHeight(root.getHeight() - 10);
                box.setPoint(new edu.agh.klaukold.common.Point(boxEdited.getDrawableShape().getBounds().right + 30, boxEdited.getDrawableShape().getBounds().top));
                if (style.equals("Default")) {
                    Text text = new Text();
                    text.setAlign(Align.CENTER);
                    text.setColor(new ColorDrawable(Color.BLACK));
                    text.setSize(13);
                    box.setShape(BlockShape.ROUNDED_RECTANGLE);
                    int color = res.getColor(R.color.light_blue);
                    box.setColor(new ColorDrawable(color));
                    box.setText(text);
                    box.setDrawableShape((GradientDrawable) res.getDrawable(R.drawable.round_rect));
                    box.setLineStyle(LineStyle.STRAIGHT);
                    box.setLineColor(Color.rgb(128, 128, 128));
                    box.setLineThickness(LineThickness.THINNEST);
                }
                boxEdited.setSelected(false);
                boxEdited = null;
                lay.invalidate();
                // deleteNote(info.id);
                lay.invalidate();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
        //  return true;
    }

    public static void addCommendUndo(Command command) {
        if (commandsUndo.size() == 10) {
            commandsUndo.removeFirst();
        }
        commandsUndo.add(command);
        menu.getItem(4).setVisible(true);
        lay.revalidate();
        lay.invalidate();
        //    lay.updateText();
    }

    public static void changeShape(Box box) {
        if (box.getShape() == BlockShape.DIAMOND) {
            box.setDrawableShape((RotateDrawable) res.getDrawable(R.drawable.diammond));
        } else if (box.getShape() == BlockShape.UNDERLINE) {
            box.setDrawableShape((GradientDrawable) res.getDrawable(R.drawable.underline));
        } else if (box.getShape() == BlockShape.NO_BORDER) {
            box.setDrawableShape((GradientDrawable) res.getDrawable(R.drawable.no_border));
        } else if (box.getShape() == BlockShape.ELLIPSE) {
            box.setDrawableShape((GradientDrawable) res.getDrawable(R.drawable.elipse));
        } else if (box.getShape() == BlockShape.RECTANGLE) {
            box.setDrawableShape((GradientDrawable) res.getDrawable(R.drawable.rect));
        } else if (box.getShape() == BlockShape.ROUNDED_RECTANGLE) {
            box.setDrawableShape((GradientDrawable) res.getDrawable(R.drawable.round_rect));
        }
    }
}
