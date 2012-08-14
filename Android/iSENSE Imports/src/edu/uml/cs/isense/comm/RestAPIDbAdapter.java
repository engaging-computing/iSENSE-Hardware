package edu.uml.cs.isense.comm;

import java.util.ArrayList;

import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.objects.Person;
import edu.uml.cs.isense.objects.Session;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Cached version of iSENSE Database to be used as a backup in case there is no
 * connectivity.
 * 
 * Version 2.0
 * 
 * @author iSENSE Android-Development Team including Mike Stowell, Nick Ver
 *         Voort, Jeremy Poulin, and James Dalphond
 */
public class RestAPIDbAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_EXPERIMENT_ID = "experiment_id";
	public static final String KEY_OWNER_ID = "owner_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_TIMECREATED = "timecreated";
	public static final String KEY_TIMEMODIFIED = "timemodified";
	public static final String KEY_DEFAULT_READ = "default_read";
	public static final String KEY_DEFAULT_JOIN = "default_join";
	public static final String KEY_FEATURED = "featured";
	public static final String KEY_RATING = "rating";
	public static final String KEY_RATING_VOTES = "votes";
	public static final String KEY_HIDDEN = "hidden";
	public static final String KEY_FIRSTNAME = "firstname";
	public static final String KEY_LASTNAME = "lastname";
	public static final String KEY_PROVIDER_URL = "provider_url";
	public static final String KEY_FIELD_ID = "field_id";
	public static final String KEY_FIELD_NAME = "field_name";
	public static final String KEY_TYPE_ID = "type_id";
	public static final String KEY_TYPE_NAME = "type_name";
	public static final String KEY_UNIT_ABBREVIATION = "unit_abbreviation";
	public static final String KEY_UNIT_ID = "unit_id";
	public static final String KEY_UNIT_NAME = "unit_name";
	public static final String KEY_TAGS = "tags";
	public static final String KEY_SESSION_ID = "session_id";
	public static final String KEY_STREET = "street";
	public static final String KEY_CITY = "city";
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_DEBUG_DATA = "debug_data";
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_PICTURE = "picture";
	public static final String KEY_EXPERIMENT_COUNT = "experiment_count";
	public static final String KEY_SESSION_COUNT = "session_count";
	public static final String KEY_ACTIVITY = "activity";
	public static final String KEY_ACTIVITY_FOR = "activity_for";
	public static final String KEY_NAME_PREFIX = "name_prefix";
	public static final String KEY_REQ_NAME = "req_name";
	public static final String KEY_REQ_PROCEDURE = "req_procedure";
	public static final String KEY_REQ_LOCATION = "req_location";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_CLOSED = "closed";
	public static final String KEY_EXP_IMAGE = "exp_image";
	public static final String KEY_RECOMMENDED = "recommended";
	public static final String KEY_SRATE = "srate";
	public static final String KEY_CONTRIB_COUNT = "contrib_count";
	public static final String KEY_RATING_COMP = "rating_comp";
	public static final String KEY_PRIVATE = "private";

	private static final String TAG = "DataDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "RestAPICache";
	private static final String DATABASE_TABLE_EXPERIMENTS = "experiments";
	private static final String DATABASE_TABLE_EXPERIMENT_IMAGES = "experimentImages";
	private static final String DATABASE_TABLE_EXPERIMENT_VIDEOS = "experimentVideos";
	private static final String DATABASE_TABLE_EXPERIMENT_TAGS = "experimentTags";
	private static final String DATABASE_TABLE_EXPERIMENT_FIELDS = "experimentFields";
	private static final String DATABASE_TABLE_SESSIONS = "sessions";
	private static final String DATABASE_TABLE_PEOPLE = "people";

	private static final int DATABASE_VERSION = 2;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE_PEOPLE = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_PEOPLE
			+ " ("
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_USER_ID
			+ " integer, "
			+ KEY_FIRSTNAME
			+ " text not null, "
			+ KEY_PICTURE
			+ " text not null, "
			+ KEY_EXPERIMENT_COUNT
			+ " integer, "
			+ KEY_SESSION_COUNT
			+ " integer);";

	private static final String DATABASE_CREATE_SESSIONS = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_SESSIONS
			+ " ("
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_EXPERIMENT_ID
			+ " integer, "
			+ KEY_SESSION_ID
			+ " integer, "
			+ KEY_OWNER_ID
			+ " integer, "
			+ KEY_NAME
			+ " text not null, "
			+ KEY_DESCRIPTION
			+ " text not null, "
			+ KEY_STREET
			+ " text not null, "
			+ KEY_CITY
			+ " text not null, "
			+ KEY_COUNTRY
			+ " text not null, "
			+ KEY_LATITUDE
			+ " real, "
			+ KEY_LONGITUDE
			+ " real, "
			+ KEY_TIMECREATED
			+ " text not null, "
			+ KEY_TIMEMODIFIED
			+ " text not null, "
			+ KEY_DEBUG_DATA
			+ " text not null, "
			+ KEY_FIRSTNAME
			+ " text not null, "
			+ KEY_LASTNAME
			+ " text not null, " + KEY_PRIVATE + " integer);";

	private static final String DATABASE_CREATE_EXPERIMENTS = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_EXPERIMENTS
			+ " ("
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_EXPERIMENT_ID
			+ " integer, "
			+ KEY_OWNER_ID
			+ " integer, "
			+ KEY_NAME
			+ " text not null, "
			+ KEY_DESCRIPTION
			+ " text not null, "
			+ KEY_TIMECREATED
			+ " text not null, "
			+ KEY_TIMEMODIFIED
			+ " text not null, "
			+ KEY_DEFAULT_READ
			+ " integer, "
			+ KEY_DEFAULT_JOIN
			+ " integer, "
			+ KEY_FEATURED
			+ " integer, "
			+ KEY_RATING
			+ " integer, "
			+ KEY_RATING_VOTES
			+ " integer, "
			+ KEY_HIDDEN
			+ " integer, "
			+ KEY_FIRSTNAME
			+ " text not null, "
			+ KEY_LASTNAME
			+ " text not null, "
			+ KEY_PROVIDER_URL
			+ " text not null, "
			+ KEY_ACTIVITY
			+ " integer, "
			+ KEY_ACTIVITY_FOR
			+ " integer, "
			+ KEY_NAME_PREFIX
			+ " text not null, "
			+ KEY_REQ_NAME
			+ " integer, "
			+ KEY_REQ_PROCEDURE
			+ " integer, "
			+ KEY_REQ_LOCATION
			+ " integer, "
			+ KEY_LOCATION
			+ " text not null, "
			+ KEY_CLOSED
			+ " integer, "
			+ KEY_EXP_IMAGE
			+ " text not null, "
			+ KEY_RECOMMENDED
			+ " integer, "
			+ KEY_SRATE
			+ " integer, "
			+ KEY_SESSION_COUNT
			+ " integer, "
			+ KEY_TAGS
			+ " text not null, "
			+ KEY_CONTRIB_COUNT
			+ " integer, "
			+ KEY_RATING_COMP
			+ " text not null);";

	private static final String DATABASE_CREATE_EXPERIMENT_IMAGES = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_EXPERIMENT_IMAGES
			+ " ("
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_EXPERIMENT_ID
			+ " integer, " + KEY_PROVIDER_URL + " text not null);";

	private static final String DATABASE_CREATE_EXPERIMENT_VIDEOS = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_EXPERIMENT_VIDEOS
			+ " ("
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_EXPERIMENT_ID
			+ " integer, " + KEY_PROVIDER_URL + " text not null);";

	private static final String DATABASE_CREATE_EXPERIMENT_TAGS = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_EXPERIMENT_TAGS
			+ " ("
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_EXPERIMENT_ID
			+ " integer, " + KEY_TAGS + " text not null);";

	private static final String DATABASE_CREATE_EXPERIMENT_FIELDS = "CREATE TABLE IF NOT EXISTS "
			+ DATABASE_TABLE_EXPERIMENT_FIELDS
			+ " ("
			+ KEY_ROWID
			+ " integer primary key autoincrement, "
			+ KEY_EXPERIMENT_ID
			+ " integer, "
			+ KEY_FIELD_ID
			+ " integer, "
			+ KEY_FIELD_NAME
			+ " text not null, "
			+ KEY_TYPE_ID
			+ " integer, "
			+ KEY_TYPE_NAME
			+ " text not null, "
			+ KEY_UNIT_ABBREVIATION
			+ " text not null, "
			+ KEY_UNIT_ID + " integer, " + KEY_UNIT_NAME + " text not null);";

	private final Context mContext;

	/**
	 * Internal class that helps creat an internal Database in case there is no
	 * data connectivity.
	 * 
	 * @author iSENSE Android-Development Team
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_EXPERIMENTS);
			db.execSQL(DATABASE_CREATE_EXPERIMENT_IMAGES);
			db.execSQL(DATABASE_CREATE_EXPERIMENT_VIDEOS);
			db.execSQL(DATABASE_CREATE_EXPERIMENT_TAGS);
			db.execSQL(DATABASE_CREATE_EXPERIMENT_FIELDS);
			db.execSQL(DATABASE_CREATE_SESSIONS);
			db.execSQL(DATABASE_CREATE_PEOPLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_EXPERIMENTS);
			db.execSQL("DROP TABLE IF EXISTS "
					+ DATABASE_TABLE_EXPERIMENT_IMAGES);
			db.execSQL("DROP TABLE IF EXISTS "
					+ DATABASE_TABLE_EXPERIMENT_VIDEOS);
			db.execSQL("DROP TABLE IF EXISTS "
					+ DATABASE_TABLE_EXPERIMENT_FIELDS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_EXPERIMENT_TAGS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SESSIONS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PEOPLE);

			onCreate(db);
		}
	}

	RestAPIDbAdapter(Context context) {
		this.mContext = context;
	}

	/**
	 * Opens a writable Database.
	 * 
	 * @return an adapter from which you can make changes to the Database
	 * @throws SQLException
	 */
	public RestAPIDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Closes the current Database.
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Adds all images belonging to the given experiment into the Database.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @param imgList
	 *            ArrayList of images
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public long insertExperimentImages(int exp_id, ArrayList<String> imgList) {
		String images = "";
		int length = imgList.size();

		for (int i = 0; i < length; i++) {
			images += imgList.get(i) + ",";
		}

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EXPERIMENT_ID, exp_id);
		initialValues.put(KEY_PROVIDER_URL, images);

		return mDb
				.insert(DATABASE_TABLE_EXPERIMENT_IMAGES, null, initialValues);
	}

	/**
	 * Removes any images associated with an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return the number of rows affected by the delete call
	 */
	public boolean deleteExperimentImages(int exp_id) {
		return mDb.delete(DATABASE_TABLE_EXPERIMENT_IMAGES, KEY_EXPERIMENT_ID
				+ "=" + exp_id, null) > 0;
	}

	/**
	 * Gets a cursor to the first image belonging to an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return location of the first image belonging to that experiment
	 */
	public Cursor getExperimentImages(int exp_id) {
		String[] tables = new String[1];
		tables[0] = KEY_PROVIDER_URL;
		Cursor mCursor = mDb.query(true, DATABASE_TABLE_EXPERIMENT_IMAGES,
				tables, KEY_EXPERIMENT_ID + " = " + exp_id, null, null, null,
				null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Adds all videos belonging to the given experiment into the Database.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @param vidList
	 *            ArrayList of videos
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public long insertExperimentVideos(int exp_id, ArrayList<String> vidList) {
		String videos = "";
		int length = vidList.size();

		for (int i = 0; i < length; i++) {
			videos += vidList.get(i) + ",";
		}

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EXPERIMENT_ID, exp_id);
		initialValues.put(KEY_PROVIDER_URL, videos);

		return mDb
				.insert(DATABASE_TABLE_EXPERIMENT_VIDEOS, null, initialValues);
	}

	/**
	 * Removes any videos associated with an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return the number of rows affected by the delete call
	 */
	public boolean deleteExperimentVideos(int exp_id) {
		return mDb.delete(DATABASE_TABLE_EXPERIMENT_VIDEOS, KEY_EXPERIMENT_ID
				+ "=" + exp_id, null) > 0;
	}

	/**
	 * Gets a cursor to the first video belonging to an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return location of the first video belonging to that experiment
	 */
	public Cursor getExperimentVideos(int exp_id) {
		String[] tables = new String[1];
		tables[0] = KEY_PROVIDER_URL;
		Cursor mCursor = mDb.query(true, DATABASE_TABLE_EXPERIMENT_VIDEOS,
				tables, KEY_EXPERIMENT_ID + " = " + exp_id, null, null, null,
				null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Adds all tags belonging to the given experiment into the Database.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @param tags
	 *            String of tags
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public long insertExperimentTags(int exp_id, String tags) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EXPERIMENT_ID, exp_id);
		initialValues.put(KEY_TAGS, tags);

		return mDb.insert(DATABASE_TABLE_EXPERIMENT_TAGS, null, initialValues);
	}

	/**
	 * Removes any tags associated with an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return the number of rows affected by the delete call
	 */
	public boolean deleteExperimentTags(int exp_id) {
		return mDb.delete(DATABASE_TABLE_EXPERIMENT_TAGS, KEY_EXPERIMENT_ID
				+ "=" + exp_id, null) > 0;
	}

	/**
	 * Gets a cursor to the first tag belonging to an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return location of the first tag belonging to that experiment
	 */
	public Cursor getExperimentTags(int exp_id) {
		String[] tables = new String[1];
		tables[0] = KEY_TAGS;
		Cursor mCursor = mDb.query(true, DATABASE_TABLE_EXPERIMENT_TAGS,
				tables, KEY_EXPERIMENT_ID + " = " + exp_id, null, null, null,
				null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Adds all experiment fields belonging to the given experiment into the
	 * Database.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @param tags
	 *            ArrayList of ExperimentField objects
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public void insertExperimentFields(int exp_id,
			ArrayList<ExperimentField> fields) {
		int length = fields.size();

		for (int i = 0; i < length; i++) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_EXPERIMENT_ID, exp_id);
			initialValues.put(KEY_FIELD_NAME, fields.get(i).field_name);
			initialValues.put(KEY_TYPE_NAME, fields.get(i).type_name);
			initialValues.put(KEY_UNIT_ABBREVIATION,
					fields.get(i).unit_abbreviation);
			initialValues.put(KEY_UNIT_NAME, fields.get(i).unit_name);
			initialValues.put(KEY_FIELD_ID, fields.get(i).field_id);
			initialValues.put(KEY_TYPE_ID, fields.get(i).type_id);
			initialValues.put(KEY_UNIT_ID, fields.get(i).unit_id);

			mDb.insert(DATABASE_TABLE_EXPERIMENT_FIELDS, null, initialValues);
		}
	}

	/**
	 * Removes any experiment fields associated with an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return the number of rows affected by the delete call
	 */
	public boolean deleteExperimentFields(int exp_id) {
		return mDb.delete(DATABASE_TABLE_EXPERIMENT_FIELDS, KEY_EXPERIMENT_ID
				+ "=" + exp_id, null) > 0;
	}

	/**
	 * Gets a cursor to the first experiment field belonging to an experiment.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return location of the first experiment field belonging to that
	 *         experiment
	 */
	public Cursor getExperimentFields(int exp_id) {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE_EXPERIMENT_FIELDS,
				null, KEY_EXPERIMENT_ID + " = " + exp_id, null, null, null,
				null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Adds all meta-data belonging to the given experiment into the Database.
	 * 
	 * @param e
	 *            Experiment object with all the new meta data
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public long insertExperiment(Experiment e) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EXPERIMENT_ID, e.experiment_id);
		initialValues.put(KEY_OWNER_ID, e.owner_id);
		initialValues.put(KEY_NAME, e.name);
		initialValues.put(KEY_DESCRIPTION, e.description);
		initialValues.put(KEY_TIMECREATED, e.timecreated);
		initialValues.put(KEY_TIMEMODIFIED, e.timemodified);
		initialValues.put(KEY_DEFAULT_READ, e.default_read);
		initialValues.put(KEY_DEFAULT_JOIN, e.default_join);
		initialValues.put(KEY_FEATURED, e.featured);
		initialValues.put(KEY_RATING, e.rating);
		initialValues.put(KEY_RATING_VOTES, e.rating_votes);
		initialValues.put(KEY_HIDDEN, e.hidden);
		initialValues.put(KEY_FIRSTNAME, e.firstname);
		initialValues.put(KEY_LASTNAME, e.lastname);
		initialValues.put(KEY_PROVIDER_URL, e.provider_url);
		initialValues.put(KEY_ACTIVITY, e.activity);
		initialValues.put(KEY_ACTIVITY_FOR, e.activity_for);
		initialValues.put(KEY_NAME_PREFIX, e.name_prefix);
		initialValues.put(KEY_REQ_NAME, e.req_name);
		initialValues.put(KEY_REQ_LOCATION, e.req_location);
		initialValues.put(KEY_REQ_PROCEDURE, e.req_procedure);
		initialValues.put(KEY_LOCATION, e.location);
		initialValues.put(KEY_CLOSED, e.closed);
		initialValues.put(KEY_EXP_IMAGE, e.exp_image);
		initialValues.put(KEY_RECOMMENDED, e.recommended);
		initialValues.put(KEY_SRATE, e.srate);
		initialValues.put(KEY_SESSION_COUNT, e.session_count);
		initialValues.put(KEY_TAGS, e.tags);
		initialValues.put(KEY_CONTRIB_COUNT, e.contrib_count);
		initialValues.put(KEY_RATING_COMP, e.rating_comp);

		return mDb.insert(DATABASE_TABLE_EXPERIMENTS, null, initialValues);
	}

	/**
	 * Adds all meta-data belonging to the given experiments into the Database.
	 * 
	 * @param exp
	 *            ArrayList of experiments to be inserted
	 * @return id of the last row newly added (or -1 upon failure)
	 */
	public long insertExperiments(ArrayList<Experiment> exp) {
		int length = exp.size();
		long lastRow = -1;

		for (int i = 0; i < length; i++)
			lastRow = insertExperiment(exp.get(i));

		return lastRow;
	}

	/**
	 * Removes a specified experiment.
	 * 
	 * @param exp
	 *            Experiment object to be deleted
	 * @return whether or not any rows were deleted
	 */
	public boolean deleteExperiment(Experiment exp) {
		return mDb.delete(DATABASE_TABLE_EXPERIMENTS, KEY_EXPERIMENT_ID + "="
				+ exp.experiment_id, null) > 0;
	}

	/**
	 * Removes any number of specified experiments.
	 * 
	 * @param exp
	 *            ArrayList of Experiment objects
	 * @return whether or not any rows were deleted
	 */
	public boolean deleteExperiments(ArrayList<Experiment> exp) {
		int lenght = exp.size();
		boolean result = false;

		for (int i = 0; i < lenght; i++)
			result |= deleteExperiment(exp.get(i));

		return result;
	}

	/**
	 * Gets a cursor to the experiment specified.
	 * 
	 * @param exp_id
	 *            experiment id
	 * @return location of the experiment specified
	 */
	public Cursor getExperiment(int exp_id) {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE_EXPERIMENTS, null,
				KEY_EXPERIMENT_ID + " = " + exp_id, null, null, null, null,
				null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Gets a cursor to the first experiment specified.
	 * 
	 * @param page
	 *            page number of the experiments you are looking for
	 * @param count
	 *            number of experiments per page
	 * @return location of the first experiment specified
	 */
	public Cursor getExperiments(int page, int count) {
		int offset = (page - 1) * count;

		Cursor mCursor = mDb.rawQuery("SELECT * FROM "
				+ DATABASE_TABLE_EXPERIMENTS + " ORDER BY " + KEY_EXPERIMENT_ID
				+ " DESC" + " LIMIT " + count + " OFFSET " + offset, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Adds all meta-data belonging to the given session into the Database.
	 * 
	 * @param s
	 *            Session object you want to insert
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public long insertSession(Session s) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_SESSION_ID, s.session_id);
		initialValues.put(KEY_OWNER_ID, s.owner_id);
		initialValues.put(KEY_EXPERIMENT_ID, s.experiment_id);
		initialValues.put(KEY_NAME, s.name);
		initialValues.put(KEY_DESCRIPTION, s.description);
		initialValues.put(KEY_STREET, s.street);
		initialValues.put(KEY_CITY, s.city);
		initialValues.put(KEY_COUNTRY, s.country);
		initialValues.put(KEY_LATITUDE, s.latitude);
		initialValues.put(KEY_LONGITUDE, s.longitude);
		initialValues.put(KEY_TIMECREATED, s.timecreated);
		initialValues.put(KEY_TIMEMODIFIED, s.timemodified);
		initialValues.put(KEY_DEBUG_DATA, s.debug_data);
		initialValues.put(KEY_FIRSTNAME, s.firstname);
		initialValues.put(KEY_LASTNAME, s.lastname);

		return mDb.insert(DATABASE_TABLE_SESSIONS, null, initialValues);
	}

	/**
	 * Adds all meta-data belonging to the given sessions into the Database.
	 * 
	 * @param s
	 *            ArrayList of Session objects you want to insert
	 * @return id of the last row newly added (or -1 upon failure)
	 */
	public long insertSessions(ArrayList<Session> s) {
		int length = s.size();
		long lastRow = -1;

		for (int i = 0; i < length; i++)
			lastRow = insertSession(s.get(i));

		return lastRow;
	}

	/**
	 * Removes a specified session.
	 * 
	 * @param s
	 *            Session object to be removed
	 * @return whether or not any rows were deleted
	 */
	public boolean deleteSession(Session s) {
		return mDb.delete(DATABASE_TABLE_SESSIONS, KEY_SESSION_ID + "="
				+ s.session_id, null) > 0;
	}

	/**
	 * Removes specified sessions.
	 * 
	 * @param s
	 *            ArrayList of Session objects to be removed
	 * @return whether or not any rows were deleted
	 */
	public boolean deleteSessions(ArrayList<Session> s) {
		int lenght = s.size();
		boolean result = false;

		for (int i = 0; i < lenght; i++)
			result |= deleteSession(s.get(i));

		return result;
	}

	/**
	 * Gets a cursor to the session specified.
	 * 
	 * @param ses_id
	 *            session id
	 * @return location of the session specified
	 */
	public Cursor getSession(int ses_id) {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE_SESSIONS, null,
				KEY_SESSION_ID + " = " + ses_id, null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Gets a cursor to the sessions specified.
	 * 
	 * @param exp_id experiment id of the experiment that the sessions belong to
	 * @return location of the first session specified
	 */
	public Cursor getSessions(int exp_id) {
		Cursor mCursor = mDb.rawQuery("SELECT * FROM "
				+ DATABASE_TABLE_SESSIONS + " WHERE " + KEY_EXPERIMENT_ID
				+ " = " + exp_id, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Adds all data belonging to the given person into the Database.
	 * 
	 * @param p Person object to be added
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public long insertPerson(Person p) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_FIRSTNAME, p.firstname);
		initialValues.put(KEY_USER_ID, p.user_id);
		initialValues.put(KEY_PICTURE, p.picture);
		initialValues.put(KEY_SESSION_COUNT, p.session_count);
		initialValues.put(KEY_EXPERIMENT_COUNT, p.experiment_count);

		return mDb.insert(DATABASE_TABLE_PEOPLE, null, initialValues);
	}

	/**
	 * Adds all data belonging to the given people into the Database.
	 * 
	 * @param p Person object to be added
	 * @return id of the row newly added (or -1 upon failure)
	 */
	public long insertPeople(ArrayList<Person> people) {
		int length = people.size();

		for (int i = 0; i < length; i++)
			insertPerson(people.get(i));

		return 1;
	}

	/**
	 * Removes the specified person from the Database.
	 * 
	 * @param p Person object to be removed
	 * @return whether or not any rows were deleted
	 */
	public boolean deletePerson(Person p) {
		return mDb.delete(DATABASE_TABLE_PEOPLE, KEY_USER_ID + "=" + p.user_id,
				null) > 0;
	}

	/**
	 * Removes the specified people from the Database.
	 * 
	 * @param people ArrayList of People objects to be deleted
	 * @return whether or not any rows were deleted
	 */
	public boolean deletePeople(ArrayList<Person> people) {
		int lenght = people.size();
		boolean result = false;

		for (int i = 0; i < lenght; i++)
			result |= deletePerson(people.get(i));

		return result;
	}

	/**
	 * Gets a cursor to the person specified.
	 * 
	 * @param user_id user id of the person
	 * @return location of the person specified
	 */
	public Cursor getPerson(int user_id) {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE_PEOPLE, null,
				KEY_USER_ID + " = " + user_id, null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Gets a cursor to the first person specified.
	 * 
	 * @param page
	 *            page number of the people you are looking for
	 * @param count
	 *            number of people per page
	 * @return location of the first person specified
	 */
	public Cursor getPeople(int page, int count) {
		int offset = (page - 1) * count;

		Cursor mCursor = mDb.rawQuery("SELECT * FROM " + DATABASE_TABLE_PEOPLE
				+ " ORDER BY " + KEY_USER_ID + " DESC" + " LIMIT " + count
				+ " OFFSET " + offset, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

}
