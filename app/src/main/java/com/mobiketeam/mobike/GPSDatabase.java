package com.mobiketeam.mobike;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mobiketeam.mobike.utils.Crypter;
import com.mobiketeam.mobike.utils.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.mobiketeam.mobike.DatabaseStrings.*;
/**
 * This class has two important features: it has a reference to the helper object defined
 * in the inner class and contains the method that the other components of the app
 * will use to perform operations and queries on the database.
 *
 */

public class GPSDatabase
{
    private Context context;
    private DbHelper dbHelper; //the reference to the Helper Object

    public final String DBNAME="gpsdb";
    public final int DBVERSION=2;

    // The raw code to initialize the database and the TWO tableS it will use.
    public final String CREATE_TABLE_LOC="CREATE TABLE "+ TABLELOC+"("+ FIELD_ID +" INTEGER PRIMARY KEY, " +
            FIELD_LAT+" VARCHAR NOT NULL, "+ FIELD_LNG+" VARCHAR NOT NULL, "+FIELD_ALT +" VARCHAR, " +
            FIELD_TIME+" INTEGER, " + FIELD_DIST + " REAL);";

    public final String CREATE_TABLE_POI = "CREATE TABLE "+TABLEPOI+"("+FIELD_ID_POI+" INTEGER PRIMARY KEY, " +
            FIELD_LAT_POI+" VARCHAR NOT NULL, "+ FIELD_LNG_POI+" VARCHAR NOT NULL, "+ FIELD_TITLE +
            " VARCHAR NOT NULL, "+FIELD_CAT +" INTEGER);";

    public final String CREATE_TABLE_ALL_POI = "CREATE TABLE "+TABLEALLPOI+"("+FIELD_ID_POI+" INTEGER PRIMARY KEY, " +
            FIELD_LAT_POI+" VARCHAR NOT NULL, "+ FIELD_LNG_POI+" VARCHAR NOT NULL, "+ FIELD_TITLE +
            " VARCHAR NOT NULL, "+FIELD_CAT +" INTEGER);";

    private final String staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?&path=weight:5%7Ccolor:0xff0000ff%7Cenc:";
    private final int maxEncodedPoints = 100;
    private final String TAG = "GPSDatabase" ;


    /**
     * The constructor, that creates the DBHelper object.
     * @param context the context
     */
    public GPSDatabase(Context context){
        this.context = context;
        dbHelper = new DbHelper(context);
    }


    /**
     * This class manages the creation and the upgrade of the database and gives a
     * reference to the helper object to retrieve data from the actual database.
     */
    public class DbHelper extends SQLiteOpenHelper {

        /**
         * This constructor method create the object that will make possible to perform operation
         * on the database DBNAME.
         * @param context the context of the app
         */
        public DbHelper(Context context){
            super(context,DBNAME,null,DBVERSION);
        }


        /** This method is invoked only once, when it does not exists a database DBNAME.
         * It executes the raw SQLite code in CREATERDB.
         * @param db the db to create
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_LOC);
            db.execSQL(CREATE_TABLE_POI);
            db.execSQL(CREATE_TABLE_ALL_POI);

        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // to implement (maybe)
        }
    }

    /**
     * This method adds a new row to the locations table.
     * @param lat the latitude of the new location
     * @param lng the longitude of the new location
     * @param alt the latitude of the new location
     * @return some long value
     */
    public long insertRowLoc(double lat, double lng, double alt, float dist)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues value=new ContentValues();
        value.put(FIELD_LAT, lat + "");
        value.put(FIELD_LNG, lng + "");
        value.put(FIELD_ALT, alt + "");
        value.put(FIELD_DIST, dist + "");
        value.put(FIELD_TIME, System.currentTimeMillis());
        try
        {
            long l = db.insert(TABLELOC, null, value);
            db.close();
            return l;
        }
        catch (SQLiteException sqle)
        {
            db.close();
            return -2;
        }
    }

    /**
     * This method adds a new row to the Points of interest table.
     *
     * @param lat the latitude of the new POI
     * @param lng the longitude of the new POI
     * @param title the title of the new POI
     * @param cat the category of the new POI (TBD)
     * @return some long value
     */
    public long insertRowPOI(double lat, double lng, String title, int cat){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues value=new ContentValues();
        value.put(FIELD_LAT_POI, lat + "");
        value.put(FIELD_LNG_POI, lng + "");
        value.put(FIELD_TITLE, title);
        value.put(FIELD_CAT, cat + "");

        try
        {
            long l = db.insert(TABLEPOI, null, value);
            db.close();
            return l;
        }
        catch (SQLiteException sqle)
        {
            db.close();
            return -2;
        }
    }

    /**
     * This method adds a new row to the All Points of interest table.
     *
     * @param lat the latitude of the new POI
     * @param lng the longitude of the new POI
     * @param title the title of the new POI
     * @param cat the category of the new POI (TBD)
     * @return some long value
     */
    public long insertRowAllPOI(double lat, double lng, String title, int cat){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues value=new ContentValues();
        value.put(FIELD_LAT_POI, lat + "");
        value.put(FIELD_LNG_POI, lng + "");
        value.put(FIELD_TITLE, title);
        value.put(FIELD_CAT, cat + "");

        try
        {
            long l = db.insert(TABLEALLPOI, null, value);
            db.close();
            return l;
        }
        catch (SQLiteException sqle)
        {
            db.close();
            return -2;
        }
    }



    /**
     * This method performs a query for all the rows in the table TABLELOC.
     * @return cursor, a Cursor object.
     */
    private Cursor getAllRowsLoc(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(TABLELOC,
                new String[]{FIELD_ID,FIELD_LAT,FIELD_LNG,FIELD_ALT, FIELD_TIME, FIELD_DIST}, null,null, null, null, null);
        //db.close();
    }

    /**
     * This method performs a query for all the rows in the table TABLEPOI.
     * @return cursor, a Cursor object.
     */
    private Cursor getAllRowsPOI(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(TABLEPOI,
                new String[]{FIELD_ID_POI,FIELD_LAT_POI,FIELD_LNG_POI,FIELD_TITLE, FIELD_CAT}, null,null, null, null, null);
        //db.close();
    }

    /**
     * This method performs a query for all the rows in the table TABLEPOI.
     * @return cursor, a Cursor object.
     */
    private Cursor getAllRowsAllPOI(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(TABLEALLPOI,
                new String[]{FIELD_ID_POI,FIELD_LAT_POI,FIELD_LNG_POI,FIELD_TITLE, FIELD_CAT}, null,null, null, null, null);
        //db.close();
    }

    /**
     * This method deletes all the rows from the table OF locations.
     */
    public void deleteTableLoc(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLELOC);
        db.close();
    }

    /**
     * This method deletes all the rows from the table of POIs.
     */
    public void deleteTablePOI(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLEPOI);
        db.close();
    }

    public void deleteTableALLPOI(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLEALLPOI);
        db.close();
    }

    /**
     * This method creates a JSON that contains all the data in the table of locations.
     * @return JSONArray the jsonArray object representing the table
     */
    private JSONArray getLocTableInJSON(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = getAllRowsLoc();
        cursor.moveToFirst();

        JSONArray resultSet = new JSONArray();
        int totalColumn;

        while (!cursor.isAfterLast()){
            totalColumn = cursor.getColumnCount();

            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    try{
                        if( cursor.getString(i) != null ){
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else{
                            rowObject.put( cursor.getColumnName(i) ,  JSONObject.NULL );
                        }
                    }
                    catch( Exception e ){
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        Log.d(TAG, resultSet.toString() );
        return resultSet;
    }

    /**
     * This method creates a JSON that contains all the data in the table of POIs.
     * @return JSONArray the jsonArray object representing the table
     */
    public JSONArray getPOITableInJSON(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = getAllRowsPOI();
        cursor.moveToFirst();

        JSONArray resultSet = new JSONArray();
        int totalColumn;

        while (!cursor.isAfterLast()){
            totalColumn = cursor.getColumnCount();

            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    try{
                        if( cursor.getString(i) != null ){
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else{
                            rowObject.put( cursor.getColumnName(i) ,  JSONObject.NULL );
                        }
                    }
                    catch( Exception e ){
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        Log.d(TAG, resultSet.toString() );
        return resultSet;
    }

    /**
     * This method creates a JSON that contains all the data in the table of All POIs.
     * @return JSONArray the jsonArray object representing the table
     */
    public JSONArray getAllPOITableInJSON(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = getAllRowsAllPOI();
        cursor.moveToFirst();

        JSONArray resultSet = new JSONArray();
        int totalColumn;

        while (!cursor.isAfterLast()){
            totalColumn = cursor.getColumnCount();

            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    try{
                        if( cursor.getString(i) != null ){
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else{
                            rowObject.put( cursor.getColumnName(i) ,  JSONObject.NULL );
                        }
                    }
                    catch( Exception e ){
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        Log.d(TAG, resultSet.toString() );
        return resultSet;
    }

    /**
     * This method performs a query on the columns of the latitude and the longitude
     * for all the rows.
     * @return an ArrayList<LatLng> containing all the recorded locations.
     */
    public ArrayList<LatLng> getAllLocations() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLELOC,
                new String[]{FIELD_LAT, FIELD_LNG}, null, null, null, null, null);

        ArrayList<LatLng> returnLst = new ArrayList<>();

        cursor.moveToFirst();

        double latitude;
        double longitude;

        while (!cursor.isAfterLast()) {
            latitude = Double.parseDouble(cursor.getString(0));
            longitude = Double.parseDouble(cursor.getString(1));

            returnLst.add(new LatLng(latitude, longitude) );

            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return returnLst;
    }

    /**
     * This method gives the total length of the route, that is the distance of the
     * last point from the first one.
     * @return the total length of the path.
     */
    public float getTotalLength() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLELOC, new String[]{FIELD_DIST}, null, null, null, null, null);
        cursor.moveToLast();
        float totLen = Float.parseFloat(cursor.getString(0));
        cursor.close();
        db.close();
        return totLen;
    }

    /**
     * This method gives the duration of the route by performing the difference between
     * the timestamps of the last and the first row of the table.
     *
     * @return The duration expressed in seconds
     */
    public long getTotalDuration() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLELOC, new String[]{FIELD_TIME}, null, null, null, null, null);
        cursor.moveToFirst();
        long start = cursor.getLong(0);
        cursor.moveToLast();
        long end = cursor.getLong(0);
        cursor.close();
        db.close();
        return (end - start)/1000;
    }

    /**
     * This method takes the name and the description the user gave to the route and creates
     * a string of the route in the GPX 1.1 format
     * @param email the user email
     * @param name the name the user gave to the route
     * @param description the description the user gave to the route
     * @return a String representing the route in the GPX 1.1 format
     */
    private String getLocTableInGPX(String email, String name, String description) {
        ArrayList<LatLng> waypoints = getWayPoints();
        String gpxString = "";
        gpxString += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" +
                "<gpx\n" +
                "  xmlns=\"http://www.topografix.com/GPX/1/0\"\n" +
                "  version=\"1.0\" creator=\"MoBike Mobile App\"\n" +
                "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n";
        gpxString += "<metadata>\n"+
                "<name>"+name+"</name>\n" +
                "<desc>"+description+"</desc>\n" +
                "<author>\n"+
                "<name>"+email.substring(0, email.indexOf("@"))+"</name>\n"+
                "<email " +
                "id=\""+email.substring(0, email.indexOf("@"))+"\""+
                " domain=\""+email.substring(email.indexOf("@")+1)+"\"/>\n"+
                "</author>\n"+
                "</metadata>\n";
        double startLat  = waypoints.get(0).latitude;
        double endLat = waypoints.get(waypoints.size()-1).latitude;
        double startLng  = waypoints.get(0).longitude;
        double endLng = waypoints.get(waypoints.size()-1).longitude;

        gpxString += "<wpt lat=\"" + startLat + "\" lon=\"" + startLng + "\">"+
                "<name>Start</name></wpt>\n";
        double wlat, wlng;

        gpxString += "<rte>\n";

        for(int j = 0; j < waypoints.size(); j++){
            wlat = waypoints.get(j).latitude;
            wlng = waypoints.get(j).longitude;
            gpxString += "<rtept lat=\"" + wlat + "\" lon=\"" + wlng + "\"></rtept>\n";
        }

        gpxString += "</rte>\n";

        gpxString += "<wpt lat=\"" + endLat + "\" lon=\"" + endLng + "\">"+
                "<name>End</name></wpt>\n";


        gpxString += "\n<trk><name>"+name+"</name>\n" +
                "<desc>"+description+"</desc>\n" +
                "<trkseg>\n";


        JSONArray array = getLocTableInJSON();
        String lat, lng, alt;
        long time;
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject row = array.getJSONObject(i);
                lat = row.getString(FIELD_LAT);
                lng = row.getString(FIELD_LNG);
                alt = row.getString(FIELD_ALT);
                time = row.getLong(FIELD_TIME);

                gpxString += "<trkpt lat=\"" + lat + "\" lon=\"" + lng + "\"><ele>" +
                        alt + "</ele><time>"+millisTimeToStr(time)+"</time></trkpt>\n";
            } catch (JSONException e) { e.printStackTrace(); }
        }

        gpxString += "</trkseg>\n" +
                "</trk>\n" +
                "</gpx>";
        return gpxString;
    }

    /**
     * This method gets all the points in the database and returns a subset of
     * that list, which are the waypoints of the route.
     * @return A list of waypoints of a route.
     */
    public ArrayList<LatLng> getWayPoints(){

        ArrayList<LatLng> waypoints = new ArrayList<>();
        ArrayList<LatLng> allPoints = getAllLocations();
        if (allPoints.size()<21){ return allPoints; }

        int interval = allPoints.size()/19;
        int index = 0;

        for(int i = 0; i<allPoints.size()-1; i++){

            if (index == 0){
                waypoints.add(allPoints.get(i));
            }
            index++;
            if (index==interval) { index = 0; }
        }

        waypoints.add(allPoints.get(allPoints.size()-1));

        return waypoints;
    }

    /**
     * This method gets a gpx file and returns the list of the TrackPoints in that file.
     * @param gpxString A string representing a gpx file.
     * @return A list of LatLng objects.
     */
    public ArrayList<LatLng> gpxToMapPoints(String gpxString) {
        ArrayList<LatLng> list = new ArrayList<>();
        double latitude;
        double longitude;
        LatLng couple;
        String[] array = gpxString.split("trkpt ");
        Log.v(TAG, "array length: "+ array.length);
        for (int i = 0; i < array.length; i++) {
            if(array[i].startsWith("lat=") ){
                String lat = array[i].substring(array[i].indexOf("lat=")+5 , array[i].indexOf("lon=")-2);
                String lon = array[i].substring(array[i].indexOf("lon=")+5, array[i].indexOf("lon=")+13);

                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lon);
                couple = new LatLng(latitude, longitude);
                list.add(couple);
            }
        }

        return list;
    }

    /**
     * This method is invoked when the user saves the route.
     * This method creates the JSONObject representing the json file to be sent using
     * the REST protocol.
     * @param email the user email
     * @param name the name the user gave to the route
     * @param description the description the user gave to the route
     * @return the JSONObject containing all the informations on the route.
     */
    public JSONObject exportRouteInJson(String email, String name, String description, String difficulty, String bends, String type, String startLocation, String endLocation){
        JSONObject jsonObject = new JSONObject(), route = new JSONObject(), user = new JSONObject();
        Crypter crypter = new Crypter();

        try{
            route.put("name", name);
            route.put("description", description);
            SharedPreferences sharedPreferences = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
            int userID = sharedPreferences.getInt(LoginActivity.ID, 0);
            String nickname = sharedPreferences.getString(LoginActivity.NICKNAME, "");
            JSONObject creator = new JSONObject();
            creator.put("id", userID);
            creator.put("nickname", nickname);
            route.put("owner", creator);
            //creation date
            String creationDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            route.put("uploaddate", creationDate);
            route.put("length", getTotalLength());
            route.put("duration", getTotalDuration());
            route.put("difficulty", difficulty);
            route.put("bends", bends);
            route.put("type", type);
            route.put("startlocation", startLocation);
            route.put("endlocation", endLocation);
            //route.put("imgUrl", getEncodedPolylineURL());
            route.put("gpxString", getLocTableInGPX(email, name, description));

            user.put("id", userID);
            user.put("nickname", nickname);

            jsonObject.put("user", crypter.encrypt(user.toString()));
            jsonObject.put("route", crypter.encrypt(route.toString()));

            Log.v(TAG, "user json: " + user.toString());
            Log.v(TAG, "route json: " + route.toString());
            Log.v(TAG, "imgUrl" + getEncodedPolylineURL());
        }
        catch(JSONException e){/*not implemented yet*/ }
        return jsonObject;
    }

    /**
     * This method converts a timestamp expressed in unix epoch time (milliseconds since 1/1/1970)
     * to a readable date/time format, in particular the xsd:datetime format of the XML standard,
     * because this one is used in GPX 1.1 format too.
     * @param millis the ecpoch time
     * @return a date in xsd:datetime format
     */
    private static String millisTimeToStr(long millis){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return format.format(calendar.getTime());
    }

    /**
     * This method creates a reference to the database.
     * @throws SQLException
     */
    public void open() throws SQLException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //return true;
    }


    public void close(){
        dbHelper.close();
    }

    public String getEncodedPolylineURL() {
        ArrayList<LatLng> input = getAllLocations(), result = new ArrayList<>();
        if (input.size() < maxEncodedPoints) {
            result = input;
        } else {
            int n = (input.size()/maxEncodedPoints) + 1;
            for (int i = 0; i < input.size() - 1; i++) {
                if (i%n == 0)
                    result.add(input.get(i));
            }
        }
        return staticMapURL + PolyUtil.encode(result) + "&size=100x100";
    }
}