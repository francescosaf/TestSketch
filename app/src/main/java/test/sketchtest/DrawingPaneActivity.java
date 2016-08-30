package test.sketchtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;

import test.sketchtest.constants.DrawingViewAction;
import test.sketchtest.domain.DrawablePath;
import test.sketchtest.domain.Sketch;
import test.sketchtest.utilities.DrawingUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DrawingPaneActivity extends Activity implements OnTouchListener,
        OnGlobalLayoutListener {

    // Used to pass the signature IN and OUT of the activity
    // as bad as it is, it's the fastest way to interact with an activity
    // using big amounts of data in the same process
    private static JSONObject diagramIn = null;
    private static JSONObject diagramOut = null;

    public static void setDiagram(JSONObject diagram) {
        diagramIn = diagram;
    }

    public static void setStorageDirPath(String path){
        storageDirPath = path;
    }
    public static JSONObject getSignature() {
        return diagramOut;
    }

    DrawingViewAction drawingMode;
    DrawingViewAction prevMode;

    Paint paint = new Paint();
    Paint textPaint = new Paint();
    Paint squarePaint = new Paint();
    Paint tempPaint = new Paint();

    Point startPoint = new Point();
    Point endPoint = new Point();

    Boolean Initialized = false;

    Button currentButton;

    DrawablePath path = new DrawablePath();

    Boolean initialized = false;
    Boolean isMoving = false;

    ImageView imageView;
    Bitmap bitmap;
    Canvas bitmapCanvas;

    ArrayList<DrawablePath> paths = new ArrayList<DrawablePath>();
    ArrayList<DrawablePath> undonePaths = new ArrayList<DrawablePath>();

    private Timer longpressTimer;
    private TimerTask timerTask;

    private int LONG_PRESS_TRESHOLD = 150;

    float TRESHOLD = 10.0f;
    static final String PP_PIC_PATH = "forms/pictures/";
    public static String storageDirPath;
    static final int GRID_SIZE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawing_pane);

        // RelativeLayout outerLayout = (RelativeLayout)
        // findViewById(R.id.relativeLayout);
        // ViewTreeObserver vto = outerLayout.getViewTreeObserver();
        // vto.addOnGlobalLayoutListener(this);

        imageView = (ImageView) this.findViewById(R.id.drawingImageView);
        // imageView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        // TODO Auto-generated method stub
        if (Initialized) {
            return;
        }
        Initialized = true;

        //setup the gridViewImage
        ImageView griImgView = (ImageView) this.findViewById(R.id.gridView);
        Bitmap bitmap = Bitmap.createBitmap(griImgView.getWidth(),
                griImgView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(bitmap);
        griImgView.setImageBitmap(bitmap);

        Paint gridPaint = new Paint();
        gridPaint.setStrokeWidth(1.0f);
        gridPaint.setColor(Color.BLACK);

        int width = griImgView.getWidth();
        int height = griImgView.getHeight();

        for (int i = 0; i<= width; i+=GRID_SIZE){
            bitmapCanvas.drawLine(i, 0, i, height, paint);
        }
        for (int y = 0; y<= height; y+=GRID_SIZE){
            bitmapCanvas.drawLine(0, y, width, y, paint);
        }

        initialize();
        initializeFromJson();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawing_pane, menu);
        return true;
    }

    void initialize() {
        final float MYTEXTSIZE = 20.0f;
        final float scale = getResources().getDisplayMetrics().density;
        final float textSizePx = (int) (MYTEXTSIZE * scale + 0.5f);

        if (initialized)
            return;

        initialized = true;

        bitmap = Bitmap.createBitmap(imageView.getWidth(),
                imageView.getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        imageView.setImageBitmap(bitmap);

        imageView.setOnTouchListener(this);

        drawingMode = DrawingViewAction.Curved;

        textPaint.setTextSize(textSizePx);
        textPaint.setColor(Color.BLACK);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(5.0f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setTextSize(textSizePx);
        paint.setColor(Color.BLACK);

        squarePaint.setAntiAlias(true);
        squarePaint.setStrokeWidth(5.0f);
        squarePaint.setStyle(Paint.Style.STROKE);

        tempPaint.setTextSize(textSizePx);
        tempPaint.setColor(Color.GRAY);

        toggleButtonEnabled();
    }

    void initializeFromJson() {
        try {
            JSONArray linePaths = diagramIn.getJSONArray("paths");
            JSONArray texts = diagramIn.getJSONArray("texts");
            JSONArray rects = diagramIn.getJSONArray("rects");

            paths = new ArrayList<DrawablePath>();
            undonePaths = new ArrayList<DrawablePath>();

            // parse the lines
            for (int i = 0; i < linePaths.length(); i++) {
                JSONArray currentPoints = linePaths.getJSONObject(i)
                        .getJSONArray("points");
                int pathType = currentPoints.length() == 2 ? DrawablePath.STRAIGHT_LINE
                        : DrawablePath.CURVED;
                DrawablePath currentPath = new DrawablePath(pathType);
                for (int y = 0; y < currentPoints.length(); y++) {
                    Point currentPoint = new Point(
                            ((JSONArray) currentPoints.get(y)).getInt(0),
                            ((JSONArray) currentPoints.get(y)).getInt(1));
                    if (y == 0) {
                        currentPath.moveTo(currentPoint.x, currentPoint.y);
                    } else {
                        currentPath.lineTo(currentPoint.x, currentPoint.y);
                    }
                    currentPath.addPoints(currentPoint);
                }
                this.paths.add(new DrawablePath(currentPath));
            }

            // parse the texts
            for (int i = 0; i < texts.length(); i++) {
                JSONObject currentJsonPath = texts.getJSONObject(i);
                DrawablePath currentPath = new DrawablePath(DrawablePath.TEXT);

                String pathText = currentJsonPath.getString("text");
                Point topLeft = new Point(
                        ((JSONArray) currentJsonPath.get("topLeft")).getInt(0),
                        ((JSONArray) currentJsonPath.get("topLeft")).getInt(1));
                int fontSize = currentJsonPath.getInt("fontSize");

                textPaint.getTextPath(pathText, 0, pathText.length(),
                        topLeft.x, topLeft.y, currentPath);

                currentPath.setText(pathText, topLeft, textPaint);
                this.paths.add(currentPath);
            }

            // parse the rects
            for (int i = 0; i < rects.length(); i++) {
                JSONObject currentJsonPath = rects.getJSONObject(i);
                DrawablePath currentPath = new DrawablePath(DrawablePath.RECT);

                Point topLeft = new Point(
                        ((JSONArray) currentJsonPath.get("topLeft")).getInt(0),
                        ((JSONArray) currentJsonPath.get("topLeft")).getInt(1));
                Point bottomRight = new Point(
                        ((JSONArray) currentJsonPath.get("bottomRight"))
                                .getInt(0),
                        ((JSONArray) currentJsonPath.get("bottomRight"))
                                .getInt(1));

                currentPath.addRect(topLeft.x, topLeft.y, bottomRight.x,
                        bottomRight.y, Direction.CW);

                currentPath
                        .setRectangle(topLeft, bottomRight, this.squarePaint);

                this.paths.add(currentPath);
            }

            this.invalidateImageView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveButtonClicked(View v) {
        Sketch sketch = new Sketch(this.paths);
        int result = RESULT_OK;

        try {
            diagramOut = sketch.serialize();

            File file = new File(storageDirPath.replace("file://", ""), UUID.randomUUID().toString() + ".png");
            file.createNewFile();
            OutputStream stream = new FileOutputStream(file.getAbsolutePath());
            /* Write bitmap to file using JPEG or PNG and 80% quality hint for JPEG. */
            bitmap.compress(CompressFormat.PNG, 80, stream);
            stream.close();

            diagramOut.put("imageFilePath", "file://"+file.getAbsolutePath());

        } catch (Exception e) {
            if (!(e instanceof IOException)){
                result = RESULT_CANCELED;
            }
            e.printStackTrace();
        } finally{
            setResult(result);
            finish();
        }
    }

    public void cancelButtonClicked(View v) {
        Sketch sketch = new Sketch(this.paths);

        try {
            diagramOut = sketch.serialize();
            setResult(this.RESULT_CANCELED);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearButtonClicked(View v) {
        clearBitmap();

        paths.clear();

        undonePaths.clear();

        imageView.invalidate();

        toggleButtonEnabled();
    }

    void clearBitmap() {
        bitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    public void toolButtonClicked(View v) {

        if (currentButton != null) {
            currentButton.setSelected(false);
        }

        currentButton = (Button) v;

        currentButton.setSelected(true);

        if (v == null || currentButton == null)
            return;

        int buttonId = currentButton.getId();

        switch (buttonId) {
            case R.id.curvedLineButton:
                drawingMode = DrawingViewAction.Curved;
                break;
            case R.id.straightLineButton:
                drawingMode = DrawingViewAction.Straight;
                break;
            case R.id.textInputButton:
                drawingMode = DrawingViewAction.Text;
                break;
            case R.id.squareButton:
                drawingMode = DrawingViewAction.Square;
                break;
            case R.id.eraseButton:
                drawingMode = DrawingViewAction.Erase;
                break;
        }

        Toast.makeText(getApplicationContext(), drawingMode.toString(),
                Toast.LENGTH_SHORT).show();
    }

    public boolean onTouch(View v, MotionEvent event) {

        v.performClick();

        int action = event.getAction();

        endPoint = new Point((int) event.getX(), (int) event.getY());

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                startPoint = new Point((int) event.getX(), (int) event.getY());

                this.InitializeLongPressTask();

                path = new DrawablePath();

                path.moveTo(startPoint.x, startPoint.y);

                isMoving = false;
                break;
            case MotionEvent.ACTION_MOVE:

                if (!isMoving
                        && DrawingUtility.DistanceToPoint(startPoint, endPoint) >= 5.00f) {
                    isMoving = true;
                    timerTask.cancel();
                }

                if (drawingMode == DrawingViewAction.Erase) {
                    this.erasePath(endPoint);
                } else if (drawingMode == DrawingViewAction.Move) {
                    path.moveTo(endPoint.x, endPoint.y);
                    path.setText(path.getText(), endPoint, tempPaint);
                    drawTemporaryPath(tempPaint);
                } else {
                    undonePaths.clear();

                    if (drawingMode == DrawingViewAction.Straight) {
                        path.reset();
                        path.moveTo(startPoint.x, startPoint.y);
                        path.lineTo(endPoint.x, endPoint.y);
                        drawTemporaryPath(paint);
                    } else if (drawingMode == DrawingViewAction.Curved) {
                        path.lineTo(endPoint.x, endPoint.y);
                        path.addPoints(endPoint);
                        drawTemporaryPath(paint);
                    } else if (drawingMode == DrawingViewAction.Square) {
                        path.reset();
                        path.addRect(startPoint.x, startPoint.y, endPoint.x,
                                endPoint.y, Direction.CW);
                        drawTemporaryPath(squarePaint);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:

                timerTask.cancel();

                if (drawingMode == DrawingViewAction.Curved) {

                    path.addPoints(endPoint);

                    path.lineTo(endPoint.x, endPoint.y);

                    path.setPathType(DrawablePath.CURVED);

                    paths.add(path);

                    invalidateImageView();
                } else if (drawingMode == DrawingViewAction.Straight) {

                    path.addPoints(startPoint);

                    path.addPoints(endPoint);

                    path.lineTo(endPoint.x, endPoint.y);

                    path.setPathType(DrawablePath.STRAIGHT_LINE);

                    paths.add(path);

                    invalidateImageView();
                } else if (drawingMode == DrawingViewAction.Text) {
                    promptUserInput();
                } else if (drawingMode == DrawingViewAction.Square) {
                    if (endPoint.x > startPoint.x && endPoint.y < startPoint.y) {
                        path.setRectangle(new Point(startPoint.x, endPoint.y),
                                new Point(endPoint.x, startPoint.y), paint);
                    } else if (endPoint.x < startPoint.x
                            && endPoint.y > startPoint.y) {
                        path.setRectangle(new Point(endPoint.x, startPoint.y),
                                new Point(startPoint.x, endPoint.y), paint);
                    } else {
                        path.setRectangle(startPoint, endPoint, paint);
                    }
                    paths.add(path);
                } else if (drawingMode == DrawingViewAction.Move) {
                    drawText(path.getText());
                    drawingMode = prevMode;
                }

                path = new DrawablePath();
                break;
            default:
                return false;
        }

        toggleButtonEnabled();
        return true;
    }

    void InitializeLongPressTask() {
        longpressTimer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handleLongPress();
            }
        };
        longpressTimer.schedule(timerTask, LONG_PRESS_TRESHOLD);
    }

    void handleLongPress() {

        DrawablePath nearestTextPath = getClosestPath(endPoint,
                DrawablePath.TEXT);

        if (nearestTextPath != null && paths.remove(nearestTextPath)) {

            prevMode = drawingMode;

            drawingMode = DrawingViewAction.Move;

            path = new DrawablePath(nearestTextPath);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawTemporaryPath(tempPaint);
                }
            });
        }
    }

    void erasePath(Point touchPoint) {

        DrawablePath foundPath = getClosestPath(touchPoint);

        if (foundPath != null) {
            paths.remove(foundPath);

            undonePaths.add(foundPath);

            this.invalidateImageView();

            toggleButtonEnabled();
        }
    }

    DrawablePath getClosestPath(Point touchPoint, int pathType) {
        Point prevpoint = null;

        for (DrawablePath p : paths) {
            if (pathType > -1 && p.getPathType() != pathType)
                continue;
            for (Point pt : p.getPoints()) {

                float distanceSq = TRESHOLD * TRESHOLD + 1;

                if (p.getPoints().size() <= 1) {
                    distanceSq = (float) DrawingUtility.DistanceToPoint(pt,
                            touchPoint);
                } else if (prevpoint != null) {
                    distanceSq = (float) DrawingUtility.DistanceToSegmentSq(
                            prevpoint, pt, touchPoint);
                }

                if (distanceSq <= TRESHOLD * TRESHOLD) {
                    if (pathType < 0)
                        return p;

                    else if (p.getPathType() == pathType) {
                        return p;
                    }
                }

                prevpoint = pt;
            }
        }
        return null;

    }

    DrawablePath getClosestPath(Point touchPoint) {
        return this.getClosestPath(touchPoint, -1);
    }

    void drawTemporaryPath(Paint p) {
        invalidateImageView();
        if (path.getPathType() == DrawablePath.TEXT) {
            Point tPoint = path.getPoints().get(0);
            bitmapCanvas.drawText(path.getText(), tPoint.x, tPoint.y, p);
        } else {
            bitmapCanvas.drawPath(path, p);
        }
    }

    void invalidateImageView() {
        clearBitmap();

        for (DrawablePath p : paths) {
            if (p.getPathType() == DrawablePath.RECT) {
                bitmapCanvas.drawPath(p, squarePaint);
            } else if (p.getPathType() == DrawablePath.TEXT)
                bitmapCanvas.drawPath(p, textPaint);
            else {
                bitmapCanvas.drawPath(p, paint);
            }
        }

        imageView.invalidate();
    }

    public void onClickUndo(View v) {

        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidateImageView();
        }
        toggleButtonEnabled();
    }

    public void onClickRedo(View v) {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidateImageView();
        }
        toggleButtonEnabled();
    }

    void toggleButtonEnabled() {
        Button redoButton = (Button) findViewById(R.id.redoButton);
        Button undoButton = (Button) findViewById(R.id.undoButton);

        redoButton.setEnabled(undonePaths.size() > 0);
        undoButton.setEnabled(paths.size() > 0);
    }

    void drawText(String text) {

        path = new DrawablePath();

        textPaint.getTextPath(text, 0, text.length(), endPoint.x, endPoint.y,
                path);

        path.setText(text, endPoint, textPaint);

        paths.add(path);

        path = new DrawablePath();

        invalidateImageView();
    }

    void MoveTextPath() {

    }

    private void promptUserInput() {

        Activity host = (Activity) this;

        AlertDialog.Builder builder = new AlertDialog.Builder(host);
        builder.setTitle("Enter text to display");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                drawText(input.getText().toString());
                toggleButtonEnabled();
            }
        });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }
}