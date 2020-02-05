package com.premature.floscript.scripts.ui.collection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.premature.floscript.MainActivity;
import com.premature.floscript.R;
import com.premature.floscript.scripts.logic.Script;

import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Created by martin on 21/01/15.
 * <p/>
 * This activity presents a selection of scripts for the user to choose from.
 */
public class ScriptCollectionActivity extends AppCompatActivity implements ScriptCollectionPageFragment.ScriptCollectionCallbackInterface {

    public static final String SCRIPT_PARAM = "SCRIPT_PARAM";
    public static final String SCRIPT_TO_EDIT_PARAM = "SCRIPT_TO_EDIT_PARAM";
    public static final String DIAGRAM_NAME_PARAM = "DIAGRAM_PARAM";

    // optional script that was passed in
    @Nullable
    private Script mScriptToEdit = null;
    private boolean isEditingMode = false;

    @BindString(R.string.scriptCollectionTitle)
    String scriptCollectionTitle;
    @BindString(R.string.basicPage)
    String basicPageTitle;
    @BindString(R.string.advancedPage)
    String advancedPageTitle;

    private ScriptCollectionPageAdapter pagerAdapter;

    /* ******************** */
    /* LIFECYCLE CALLBACKS */
    /* ****************** */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.script_collection_activity);

        ButterKnife.bind(this);

        Intent startingIntent = getIntent();
        if (startingIntent.getExtras() != null) {
            Bundle extras = startingIntent.getExtras();
            mScriptToEdit = extras.getParcelable(SCRIPT_TO_EDIT_PARAM);
            isEditingMode = mScriptToEdit != null;
        }

        ViewPager pager = (ViewPager) findViewById(R.id.vpPager);
        pagerAdapter = new ScriptCollectionPageAdapter(getSupportFragmentManager(), new String[] {basicPageTitle, advancedPageTitle});
        pager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle(scriptCollectionTitle);
    }

    @Override
    public void scriptSelected(Script script) {
        Intent data = new Intent(getApplicationContext(), MainActivity.class);
        data.putExtra(SCRIPT_PARAM, script);
        setResult(0, data);
        finish();
    }

    @Override
    public boolean isEditingMode(ScriptCollectionPageType pageType) {
        return isEditingMode && pageType.hasScriptType(mScriptToEdit.getType());
    }

    @Override
    public Script getEditingScript() {
        return mScriptToEdit;
    }
}
