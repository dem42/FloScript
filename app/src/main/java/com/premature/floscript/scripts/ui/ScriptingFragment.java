package com.premature.floscript.scripts.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.premature.floscript.R;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.FloDbHelper;
import com.premature.floscript.events.DiagramValidationEvent;
import com.premature.floscript.scripts.logic.DiagramToScriptCompiler;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.logic.ScriptCompilationException;
import com.premature.floscript.scripts.logic.ScriptEngine;
import com.premature.floscript.scripts.logic.ScriptExecutionException;
import com.premature.floscript.scripts.logic.StringResolver;
import com.premature.floscript.scripts.ui.diagram.ArrowUiElement;
import com.premature.floscript.scripts.ui.diagram.Diagram;
import com.premature.floscript.scripts.ui.diagram.DiagramEditorView;
import com.premature.floscript.scripts.ui.diagram.DiagramUtils;
import com.premature.floscript.scripts.ui.diagram.DiamondUiElement;
import com.premature.floscript.scripts.ui.diagram.LogicBlockUiElement;
import com.premature.floscript.util.FloBus;
import com.squareup.otto.Subscribe;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.LAYER_TYPE_SOFTWARE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScriptingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class ScriptingFragment extends Fragment implements SaveDialog.OnSaveDialogListener,
        LoadDialog.OnLoadDialogListener {
    private static final String TAG = "SCRIPT_FRAG";

    private LogicBlockUiElement mLogicBlockElement;
    private DiamondUiElement mDiamondElement;
    private ArrowUiElement mArrowElement;

    @BindView(R.id.script_editor)
    DiagramEditorView mDiagramEditorView;
    @BindView(R.id.logic_elem_btn)
    Button mLogicElemBtn;
    @BindView(R.id.diamond_elem_btn)
    Button mDiamondElemBtn;
    @BindView(R.id.arrow_elem_btn)
    Button mArrowElemBtn;

    @BindString(R.string.error_compile_diagram)
    String ERROR_COMPILING_DIAGRAM_POPUP_TITLE;
    @BindString(R.string.error_running_diagram)
    String ERROR_RUNNING_DIAGRAM_POPUP_TITLE;
    @BindString(R.string.diagram_code_popup_title)
    String DIAGRAM_CODE_POPUP_TITLE;
    @BindString(R.string.failed_to_compile_msg)
    String FAILED_TO_COMPILE;

    private float mDensity;
    private StickyButtonCoordinator mBtnCoordinator;
    private DiagramDao mDiagramDao;
    private DiagramToScriptCompiler mCompiler;
    private StringResolver stringResolver;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p/>
     * We use this instead of a overloaded constructor because
     * android will call the no argument constructor when it decides to
     * recreate our fragment later. So we shouldn't use those all over the place
     * instead we should pass arguments through the setArguments method (these will
     * be retained between recreations)
     *
     * @return A new instance of fragment ScriptingFragment.
     */
    public static ScriptingFragment newInstance() {
        ScriptingFragment fragment = new ScriptingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ScriptingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // looks like there may be multiple onCreate calls but one will have the arguments
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
        init();

        // need to call this if we want to manipulate the options menu
        setHasOptionsMenu(true);

        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "inflating our menu " + menu);
        inflater.inflate(R.menu.menu_scripts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveDiagram();
                return true;
            case R.id.action_load:
                loadDiagram();
                return true;
            case R.id.action_clear:
                clearEditor();
                return true;
            case R.id.action_compile:
                Log.d(TAG, "Compiling");
                compileDiagram();
                return true;
            case R.id.action_test:
                Log.d(TAG, "Testing code");
                compileAndRunDiagram();
                return true;
            case R.id.action_admin:
                Log.d(TAG, "admin code");
                adminCode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onDiagramValidationError(DiagramValidationEvent validationEvent) {
        TextPopupDialog.showErrorPopup(getFragmentManager(), stringResolver.resolve(validationEvent.errorCode), ERROR_COMPILING_DIAGRAM_POPUP_TITLE);
    }

    @Override
    public void loadClicked(String name) {
        new LoadDiagramTask(this).execute(name);
    }

    @Override
    public void saveClicked(String name, String description) {
        final Diagram diagram = mDiagramEditorView.getDiagram();
        diagram.setOriginalName(diagram.getName());
        diagram.setName(name);
        diagram.setDescription(description);
        try {
            Script compiledDiagram = mCompiler.compile(diagram);
            diagram.setCompiledDiagram(compiledDiagram);
        } catch (ScriptCompilationException e) {
            TextPopupDialog.showErrorPopup(getActivity().getSupportFragmentManager(), FAILED_TO_COMPILE + e.getScriptCompilationMessage(stringResolver), ERROR_COMPILING_DIAGRAM_POPUP_TITLE);
        }
        new SaveDiagramTask(this, true).execute(diagram);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "Creating scripting view");
        final View view = inflater.inflate(R.layout.fragment_scripting, container, false);
        ButterKnife.bind(this, view);
        initButtons();

        stringResolver = new StringResolver(view);
        FloBus.getInstance().register(this);

        mDiagramEditorView.busRegister(true);
        mDiagramEditorView.setStringResolver(stringResolver);
        Diagram workInProgressDiagram = mDiagramDao.getDiagram(DiagramDao.WORK_IN_PROGRESS_DIAGRAM);
        if (workInProgressDiagram != null) {
            mDiagramEditorView.setDiagram(workInProgressDiagram);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroying scripting view");
        FloBus.getInstance().unregister(this);
        mDiagramEditorView.busRegister(false);
        mDiagramEditorView.setStringResolver(null);
        // make the saving non-blocking
        Diagram workInProgressDiagram = mDiagramEditorView.getDiagram();
        workInProgressDiagram.setOriginalName(workInProgressDiagram.getName());
        workInProgressDiagram.setName(DiagramDao.WORK_IN_PROGRESS_DIAGRAM);
        new SaveDiagramTask(this, false).execute(workInProgressDiagram);
    }

    // these methods are for the interaction with nested async task (dont want inner class asyncs)
    DiagramDao getDiagramDao() {
        return mDiagramDao;
    }

    // these methods are for the interaction with nested async task (dont want inner class asyncs)
    void showDiagram(Diagram diagram) {
        mDiagramEditorView.setDiagram(diagram);
    }

    private void clearEditor() {
        this.mDiagramEditorView.setDiagram(DiagramUtils.createEmptyDiagram());
    }

    //TODO: get rid of admin stuff
    private void adminCode() {
        FloDbHelper mDb = FloDbHelper.getInstance(getActivity());
        mDb.wipe();
    }

    private void compileAndRunDiagram() {
        try {
            Script script = mCompiler.compile(mDiagramEditorView.getDiagram());
            String result = new ScriptEngine(getActivity().getApplicationContext()).runScript(script);
            TextPopupDialog.showInfoPopup(getActivity().getSupportFragmentManager(), script.getSourceCode() +
                    "\n\nWith result: " + result, DIAGRAM_CODE_POPUP_TITLE);
        } catch (ScriptCompilationException e) {
            TextPopupDialog.showErrorPopup(getActivity().getSupportFragmentManager(), e.getScriptCompilationMessage(stringResolver), ERROR_COMPILING_DIAGRAM_POPUP_TITLE);
            Log.e(TAG, "Compile exception", e);
        } catch (ScriptExecutionException e) {
            TextPopupDialog.showErrorPopup(getActivity().getSupportFragmentManager(), e.getMessage(), ERROR_RUNNING_DIAGRAM_POPUP_TITLE);
            Log.e(TAG, "Execute exception", e);
        }
    }

    private void initButtons() {
        mBtnCoordinator = new StickyButtonCoordinator();
        mDiagramEditorView.setOnDiagramEditorListener(mBtnCoordinator);

        mLogicElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mLogicElemBtn.setCompoundDrawables(mLogicBlockElement.getDrawable(), null, null, null);
        mLogicElemBtn.setPadding(8, 0, 0, 0);
        StickyButtonOnTouchListener logicListener = new StickyButtonOnTouchListener(mLogicElemBtn,
                mDiagramEditorView, mBtnCoordinator) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onLogicElementClicked();
            }
        };
        mLogicElemBtn.setOnTouchListener(logicListener);
        mBtnCoordinator.registerElementButtonListener(logicListener);

        mDiamondElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mDiamondElemBtn.setCompoundDrawables(mDiamondElement.getDrawable(), null, null, null);
        mDiamondElemBtn.setPadding(8, 0, 0, 0);
        StickyButtonOnTouchListener diamondListener = new StickyButtonOnTouchListener(mDiamondElemBtn,
                mDiagramEditorView, mBtnCoordinator) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onDiamondElementClicked();
            }
        };
        mDiamondElemBtn.setOnTouchListener(diamondListener);
        mBtnCoordinator.registerElementButtonListener(diamondListener);

        mArrowElemBtn.setLayerType(LAYER_TYPE_SOFTWARE, null);
        mArrowElemBtn.setCompoundDrawables(mArrowElement.getDrawable(), null, null, null);
        StickyButtonOnTouchListener arrowListener = new StickyButtonOnTouchListener(mArrowElemBtn,
                mDiagramEditorView, mBtnCoordinator) {
            @Override
            public void doOnClick() {
                super.doOnClick();
                this.mOnElementSelectorListener.onArrowClicked();
            }
        };
        mArrowElemBtn.setOnTouchListener(arrowListener);
        mBtnCoordinator.registerElementButtonListener(arrowListener);
    }

    private void init() {
        this.mCompiler = new DiagramToScriptCompiler(getActivity());
        this.mDiagramDao = new DiagramDao(getActivity());
        this.mDensity = getResources().getDisplayMetrics().density;
        this.mLogicBlockElement = new LogicBlockUiElement(null, (int) (40 * mDensity), (int) (40 * mDensity));
        this.mDiamondElement = new DiamondUiElement(null, (int) (40 * mDensity), (int) (40 * mDensity));
        this.mArrowElement = new ArrowUiElement(null, (int) (40 * mDensity), (int) (6 * mDensity));

        // align the elements inside the buttons
        mArrowElement.advanceBy(8, 10);
    }

    private void compileDiagram() {
        DiagramToScriptCompiler compiler = new DiagramToScriptCompiler(getActivity());

        try {
            Script script = compiler.compile(mDiagramEditorView.getDiagram());
            TextPopupDialog.showInfoPopup(getActivity().getSupportFragmentManager(), script.getSourceCode(), DIAGRAM_CODE_POPUP_TITLE);
        } catch (ScriptCompilationException e) {
            TextPopupDialog.showErrorPopup(getActivity().getSupportFragmentManager(), e.getScriptCompilationMessage(stringResolver), ERROR_COMPILING_DIAGRAM_POPUP_TITLE);
            Log.e(TAG, "compile exp", e);
        }
    }

    private void loadDiagram() {
        LoadDialog dialog = new LoadDialog();
        dialog.setTargetFragment(this, 1);
        dialog.show(getActivity().getSupportFragmentManager(), "load dialog");
    }

    private void saveDiagram() {
        if (!mDiagramEditorView.isDiagramValid()) {
            return;
        }
        SaveDialog dialog = new SaveDialog();
        dialog.setTargetFragment(this, 1);
        dialog.show(getActivity().getSupportFragmentManager(), "save dialog");
    }
}
