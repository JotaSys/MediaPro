package cu.ij.jotalab.mediapro;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

import cu.ij.jotalab.mediapro.AsyncTasks.SyncDatabase;
import cu.ij.jotalab.mediapro.SearchEngine.SearchActivity;
import cu.ij.jotalab.mediapro.adapters.GridHomeAdapter;
import cu.ij.jotalab.mediapro.database.DatabaseController;
import cu.ij.jotalab.mediapro.explorer.ExplorerActivity;
import dmax.dialog.SpotsDialog;

public class DrawerMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    GridView gridHome;
    GridHomeAdapter gridHomeAdapter;
    DatabaseController controller;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_drawer_main);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Portada");
        //toolbar.setTitleMarginStart(50);

        searchView = (android.support.v7.widget.SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent goToSearch = new Intent(DrawerMainActivity.this, SearchActivity.class);
                goToSearch.putExtra("search_pattern",query);
                startActivity(goToSearch);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        controller =  new DatabaseController(this,"/");
        gridHome = (GridView) findViewById(R.id.grid_home);
        gridHomeAdapter = new GridHomeAdapter(this);
        gridHome.setAdapter(gridHomeAdapter);
        gridHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent goToDetail = new Intent(DrawerMainActivity.this, DetailActivity.class);

                TextView tv1 = (TextView) view.findViewById(R.id.tv_id_game);
                String id_game = tv1.getText().toString();
                goToDetail.putExtra("position", position);
                goToDetail.putExtra("id_game", id_game);
                startActivity(goToDetail);
            }
        });

        checkSMSStatePermission();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            tapTwooToOut();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_terms) {
            Intent goToTerms = new Intent(DrawerMainActivity.this,TermsConditionsActivity.class);
            goToTerms.putExtra("mainmenu", "terms");
            startActivity(goToTerms);
        }
        else if (id == R.id.action_help) {
            Intent goToHelp = new Intent(DrawerMainActivity.this,TermsConditionsActivity.class);
            goToHelp.putExtra("mainmenu", "help");
            startActivity(goToHelp);
        }
        else if (id == R.id.action_about) {
            Intent goToAbout = new Intent(DrawerMainActivity.this,TermsConditionsActivity.class);
            goToAbout.putExtra("mainmenu", "about");
            startActivity(goToAbout);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean close = false;
    private void tapTwooToOut()
    {
        if (close) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            { finishAffinity(); }
            else finish();
        }
        else {
            Toast to = Toast.makeText(DrawerMainActivity.this,"Presione nuevamente para salir", Toast.LENGTH_SHORT);

            close = true;
            to.show();
            final Thread t = new Thread() {
                public void run()
                {
                    try {
                        int time = 0;
                        while (time <= 5000)
                        {
                            sleep(100);
                            time += 100;
                        }
                        close = false;
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally {}
                }
            };
            t.start();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (id == R.id.nav_home) {
            Intent goToMain = new Intent(DrawerMainActivity.this,DrawerMainActivity.class);
            startActivity(goToMain);

        } else if (id == R.id.nav_sync) {

            final SpotsDialog loading = new SpotsDialog(DrawerMainActivity.this, "sync Catálogo");
            loading.show();

            final Handler handler = new Handler();
            final SyncDatabase syncDatabase= new SyncDatabase(DrawerMainActivity.this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                syncDatabase.synchronizedDataBaseFromService();
                                if (syncDatabase.getResult() !="") {
                                    Toast.makeText(DrawerMainActivity.this, syncDatabase.getResult(), Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                }
                                else
                                {
                                    Intent goToMain = new Intent(DrawerMainActivity.this,DrawerMainActivity.class);
                                    startActivity(goToMain);
                                }
                            }
                        });
                    }
                    catch (InterruptedException ie)
                    {ie.printStackTrace();}
                }
            }).start();

        }else if (id == R.id.nav_copy) {
            controller.open();
            try {
                controller.getHelper().duplicateDataBaseFromFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            controller.getHelper().close();

        } else if (id == R.id.nav_load) {
            Intent openSD = new Intent(DrawerMainActivity.this, ExplorerActivity.class);
            startActivity(openSD);

        } else if (id == R.id.nav_call) {

            try {
                Uri numero = Uri.parse("tel:" + "53147931");
                Intent intent = new Intent(Intent.ACTION_CALL, numero);
                if (ActivityCompat.checkSelfPermission(DrawerMainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "NO SE PUDO REALIZAR LA LLAMADA", Toast.LENGTH_SHORT).show();
                    return false;
                }
                startActivity(intent);
            }
            catch (ActivityNotFoundException activityException){
                Log.e("CallEx", "NO SE PUDO REALIZAR LA LLAMADA, VER LOG", activityException);}

        } else if (id == R.id.nav_sms) {
                if (checkSMSStatePermission())
                {
                    String phone = "53147931";
                    String text = "Hola, me interesa el juego:";
                    /*SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(phone, null, text , null, null);*/
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setType("vnd.android-dir/mms-sms");
                    sendIntent.putExtra("address", phone);
                    sendIntent.putExtra("sms_body", text);
                    startActivity(sendIntent);
                }
                else Toast.makeText(this, "FUNCION NO PERMITIDA", Toast.LENGTH_SHORT).show();

        }else if (id == R.id.action_adventure) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Accion-Aventura");
            startActivity(category_anuncuio);

        } else if (id == R.id.arcade_music_puzzle) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Arcade-MÃºsica-Puzzle");
            startActivity(category_anuncuio);

        } else if (id == R.id.race) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Carrera");
            startActivity(category_anuncuio);

        } else if (id == R.id.sport) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Deporte");
            startActivity(category_anuncuio);

        } else if (id == R.id.strategy) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Estrategia");
            startActivity(category_anuncuio);

        }else if (id == R.id.graphic_adv) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Graphic Adv.");
            startActivity(category_anuncuio);

        } else if (id == R.id.horror) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Horror");
            startActivity(category_anuncuio);

        } else if (id == R.id.kids) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Kids");
            startActivity(category_anuncuio);

        } else if (id == R.id.figth) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Pelea");
            startActivity(category_anuncuio);

        } else if (id == R.id.rpg) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","RPG");
            startActivity(category_anuncuio);

        }else if (id == R.id.sandbox) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Sandbox");
            startActivity(category_anuncuio);

        } else if (id == R.id.shoter) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Shooter");
            startActivity(category_anuncuio);

        } else if (id == R.id.simulator) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Simulador");
            startActivity(category_anuncuio);

        } else if (id == R.id.comming_soon) {
            Intent category_anuncuio = new Intent(DrawerMainActivity.this,CategoryActivity.class);
            category_anuncuio.putExtra("category","Proximos estrenos");
            startActivity(category_anuncuio);

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean checkSMSStatePermission() {
        boolean chek = false;
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para enviar SMS.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 225);
            chek = true;
        } else {
            Log.i("Mensaje", "Se tiene permiso para enviar SMS!");
            chek = true;
        }

        return chek;
    }
}
