package kevinkromjong.nl.tripteacher;



import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View.OnClickListener;



public class LoginActivity extends ActionBarActivity implements OnClickListener {

    private EditText usernameField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");

        //Find the inputs and button in the view
        usernameField = (EditText) findViewById(R.id.loginNameField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        Button loginButton = (Button) findViewById(R.id.loginButton);

        //Set onclicklistener on button
        loginButton.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginButton :
                if(usernameField.getText().toString().equals("admin") && passwordField.getText().toString().equals("admin")) {
                    Intent i = new Intent(this, HomeActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Je gebruikersnaam en/of wachtwoord kloppen niet", Toast.LENGTH_LONG).show();
                }
                break;

        }
    }
}
