// Copyright 2007-2014 metaio GmbH. All rights reserved.
package kevinkromjong.nl.tripteacher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.ARELInterpreterAndroidJava;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class CameraActivity extends ARViewActivity
{

    private AssetsExtracter mTask;
    private GetPOIFromDatabase mNames;

    List<HashMap<String, String>> assArray= new ArrayList<HashMap<String, String>>();


    private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;

    private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;


    /**
     * Geometries
     */
    private IGeometry mBastilleGeo;
    private IGeometry mSacreCoueurGeo;
    private IGeometry mEiffelTowerGeo;
    private IGeometry mNotreDameGeo;
    private IGeometry mArcDeTriomphGeo;

    private IRadar mRadar;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mNames = new GetPOIFromDatabase();
        mNames.execute(0);

        mTask = new AssetsExtracter();
        mTask.execute(0);

        // Set GPS tracking configuration
        boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
        Log.i("Tracking data loaded: ", "" + result);
    }

    @Override
    protected void onDestroy()
    {
        // Break circular reference of Java objects
        if (mAnnotatedGeometriesGroup != null)
        {
            mAnnotatedGeometriesGroup.registerCallback(null);
        }

        if (mAnnotatedGeometriesGroupCallback != null)
        {
            mAnnotatedGeometriesGroupCallback.delete();
            mAnnotatedGeometriesGroupCallback = null;
        }

        super.onDestroy();
    }

    @Override
    public void onDrawFrame()
    {
        if (metaioSDK != null && mSensors != null)
        {
            SensorValues sensorValues = mSensors.getSensorValues();

            float heading = 0.0f;
            if (sensorValues.hasAttitude())
            {
                float m[] = new float[9];
                sensorValues.getAttitude().getRotationMatrix(m);

                Vector3d v = new Vector3d(m[6], m[7], m[8]);
                v.normalize();

                heading = (float)(-Math.atan2(v.getY(), v.getX()) - Math.PI / 2.0);
            }

            IGeometry geos[] = new IGeometry[] {mBastilleGeo, mSacreCoueurGeo, mEiffelTowerGeo, mNotreDameGeo, mArcDeTriomphGeo};
            Rotation rot = new Rotation((float)(Math.PI / 2.0), 0.0f, -heading);
            for (IGeometry geo : geos)
            {
                if (geo != null)
                {
                    geo.setRotation(rot);
                }
            }
        }

        super.onDrawFrame();
    }

    public void onButtonClick(View v)
    {
        finish();
    }

    @Override
    protected int getGUILayout()
    {
        return R.layout.tutorial_location_based_ar;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return null;
    }

    @Override
    protected void loadContents()
    {

//        for (int i = 0; i < assArray.size(); i++) {
//            String nameOfPlace = assArray.get(i).get("name");
//
//            LLACoordinate nameOfPlace =
//
//        }

        mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
        mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
        mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);

        // Clamp geometries' Z position to range [5000;200000] no matter how close or far they are
        // away.
        // This influences minimum and maximum scaling of the geometries (easier for development).
        metaioSDK.setLLAObjectRenderingLimits(5, 200);

        // Set render frustum accordingly
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);


            // let's create LLA objects for known cities
//            LLACoordinate bastille = new LLACoordinate(51.50661, -0.130463, 0, 0);
            LLACoordinate bastille = new LLACoordinate(48.853177, -2.369194, 0, 0);
            LLACoordinate sacrecoueur = new LLACoordinate(59.913869, 10.752245, 0, 0);
            LLACoordinate eiffeltower = new LLACoordinate(48.858370, 2.294481, 0, 0);
            LLACoordinate notredame = new LLACoordinate(41.902783, 12.496366, 0, 0);
            LLACoordinate arcdetriomph = new LLACoordinate(51.507351, -0.127758, 0, 0);


        // Load some POIs. Each of them has the same shape at its geoposition. We pass a string
        // (const char*) to IAnnotatedGeometriesGroup::addGeometry so that we can use it as POI
        // title
        // in the callback, in order to create an annotation image with the title on it.
        mBastilleGeo = createPOIGeometry(bastille);
        mAnnotatedGeometriesGroup.addGeometry(mBastilleGeo, "Bastille");
        mBastilleGeo.setName("1");

        mSacreCoueurGeo = createPOIGeometry(sacrecoueur);
        mAnnotatedGeometriesGroup.addGeometry(mSacreCoueurGeo, "Sacre Coueur");
        mSacreCoueurGeo.setName("2");

        mEiffelTowerGeo = createPOIGeometry(eiffeltower);
        mAnnotatedGeometriesGroup.addGeometry(mEiffelTowerGeo, "Eiffeltoren");
        mEiffelTowerGeo.setName("3");

        mNotreDameGeo = createPOIGeometry(notredame);
        mAnnotatedGeometriesGroup.addGeometry(mNotreDameGeo, "Notre Dame");
        mNotreDameGeo.setName("4");

        mArcDeTriomphGeo = createPOIGeometry(arcdetriomph);
        mAnnotatedGeometriesGroup.addGeometry(mArcDeTriomphGeo, "Arc de Triomph");
        mArcDeTriomphGeo.setName("5");





        File metaioManModel =
                AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "TutorialLocationBasedAR/Assets/metaioman.md2");
        if (metaioManModel != null)
        {
//            mMunichGeo = metaioSDK.createGeometry(metaioManModel);
//            if (mMunichGeo != null)
//            {
//                mMunichGeo.setTranslationLLA(bastille);
//                mMunichGeo.setLLALimitsEnabled(true);
//                mMunichGeo.setScale(500);
//            }
//            else
//            {
//                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + metaioManModel);
//            }
        }

        // create radar
        mRadar = metaioSDK.createRadar();
        mRadar.setBackgroundTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                "TutorialLocationBasedAR/Assets/radar.png"));
        mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                "TutorialLocationBasedAR/Assets/yellow.png"));
        mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

        // add geometries to the radar
        mRadar.add(mBastilleGeo);
        mRadar.add(mSacreCoueurGeo);
        mRadar.add(mEiffelTowerGeo);
        mRadar.add(mNotreDameGeo);
        mRadar.add(mArcDeTriomphGeo);
    }

    private IGeometry createPOIGeometry(LLACoordinate lla)
    {
        final File path =
                AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "TutorialLocationBasedAR/Assets/ExamplePOI.obj");
        if (path != null)
        {
            IGeometry geo = metaioSDK.createGeometry(path);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(100);
            return geo;
        }
        else
        {
            MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
            Log.i("Missing files", "Missing files for POI geometry");
            return null;
        }
    }

    @Override
    protected void onGeometryTouched(final IGeometry geometry)
    {

        String naam = geometry.getName();
        Log.i("Geklikt", "Geometry selected: " + geometry);
        Intent i = new Intent(this, PointOfInterestActivity.class);
        i.putExtra("NAME_OF_MONUMENT", naam);
        startActivity(i);

        mSurfaceView.queueEvent(new Runnable()
        {

            @Override
            public void run()
            {
                mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "TutorialLocationBasedAR/Assets/yellow.png"));
                mRadar.setObjectTexture(geometry, AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "TutorialLocationBasedAR/Assets/red.png"));
                mAnnotatedGeometriesGroup.setSelectedGeometry(geometry);
            }
        });
    }

    final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback
    {
        Bitmap mAnnotationBackground, mEmptyStarImage, mFullStarImage;
        int mAnnotationBackgroundIndex;
        ImageStruct texture;
        String[] textureHash = new String[1];
        TextPaint mPaint;
        Lock geometryLock;


        Bitmap inOutCachedBitmaps[] = new Bitmap[] {mAnnotationBackground, mEmptyStarImage, mFullStarImage};
        int inOutCachedAnnotationBackgroundIndex[] = new int[] {mAnnotationBackgroundIndex};

        public MyAnnotatedGeometriesGroupCallback()
        {
            mPaint = new TextPaint();
            mPaint.setFilterBitmap(true); // enable dithering
            mPaint.setAntiAlias(true); // enable anti-aliasing
        }

        @Override
        public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData, IGeometry existingAnnotation)
        {
            if (userData == null)
            {
                return null;
            }

            if (existingAnnotation != null)
            {
                // We don't update the annotation if e.g. distance has changed
                return existingAnnotation;
            }

            String title = (String)userData; // as passed to addGeometry
            LLACoordinate location = geometry.getTranslationLLA();
            float distance = (float)MetaioCloudUtils.getDistanceBetweenTwoCoordinates(location, mSensors.getLocation());
            Bitmap thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.monument);
            try
            {
                texture =
                        ARELInterpreterAndroidJava.getAnnotationImageForPOI(title, title, distance, null, thumbnail,
                                null,
                                metaioSDK.getRenderSize(), CameraActivity.this,
                                mPaint, inOutCachedBitmaps, inOutCachedAnnotationBackgroundIndex, textureHash);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (thumbnail != null)
                    thumbnail.recycle();
                thumbnail = null;
            }

            mAnnotationBackground = inOutCachedBitmaps[0];
            mEmptyStarImage = inOutCachedBitmaps[1];
            mFullStarImage = inOutCachedBitmaps[2];
            mAnnotationBackgroundIndex = inOutCachedAnnotationBackgroundIndex[0];

            IGeometry resultGeometry = null;

            if (texture != null)
            {
                if (geometryLock != null)
                {
                    geometryLock.lock();
                }

                try
                {
                    // Use texture "hash" to ensure that SDK loads new texture if texture changed
                    resultGeometry = metaioSDK.createGeometryFromImage(textureHash[0], texture, true, false);
                }
                finally
                {
                    if (geometryLock != null)
                    {
                        geometryLock.unlock();
                    }
                }
            }

            return resultGeometry;
        }

        @Override
        public void onFocusStateChanged(IGeometry geometry, Object userData, EGEOMETRY_FOCUS_STATE oldState,
                                        EGEOMETRY_FOCUS_STATE newState)
        {
            MetaioDebug.log("onFocusStateChanged for " + (String)userData + ", " + oldState + "->" + newState);
        }
    }

    class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
    {

        @Override
        protected Boolean doInBackground(Integer... params)
        {
            try
            {
                // Extract all assets except Menu. Overwrite existing files for debug build only.
                final String[] ignoreList = {"Menu", "webkit", "sounds", "images", "webkitsec"};
                AssetsManager.extractAllAssets(getApplicationContext(), "", ignoreList, BuildConfig.DEBUG);
            }
            catch (IOException e)
            {
                MetaioDebug.printStackTrace(Log.ERROR, e);
                return false;
            }

            return true;
        }

    }


    class GetPOIFromDatabase extends AsyncTask<Integer, JSONObject, JSONArray>
    {
        private String url = "http://kevinkromjong.nl/pressurecooker/poi-information/places";

        @Override
        protected JSONArray doInBackground(Integer... params) {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jobj = jsonParser.getJSONFromUrl(url);
                JSONArray jarr = jobj.getJSONArray("places");

                for(int i = 0; i < jarr.length(); i++){

//                    HashMap<String, String> Data= new HashMap<String, String>();

//                    Data.put("header_title", jarr.getJSONObject(i).getString("header_title"));
//                    Data.put("latitude", jarr.getJSONObject(i).getString("latitude"));
//                    Data.put("longitude", jarr.getJSONObject(i).getString("longitude"));
//                    assArray.add(i, Data);

//                    Log.i("appelsap", "appelsap" + jarr.getJSONObject(i).getString("header_title"));
//                    Toast.makeText(getApplicationContext(), "header_title: " + jarr.getJSONObject(i).getString("header_title"), Toast.LENGTH_SHORT).show();
                }






                Log.i("Namen", "Namen: " + jarr);

                return jarr;
            } catch (Exception e) {
                Log.e("JSONError", "Error: " + e);
            }

            return null;
        }
    }
}
