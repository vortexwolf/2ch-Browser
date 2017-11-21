package ua.in.quireg.chan.views.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.adapters.BoardsListAdapter;
import ua.in.quireg.chan.boards.makaba.MakabaModelsMapper;
import ua.in.quireg.chan.boards.makaba.models.MakabaBoardInfo;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.GeneralUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.FavoritesEntity;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.models.presentation.SectionEntity;
import ua.in.quireg.chan.services.NavigationService;
import ua.in.quireg.chan.services.presentation.EditTextDialog;
import ua.in.quireg.chan.settings.ApplicationSettings;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class BoardsListFragment extends MvpAppCompatFragment {

    private static final Pattern boardCodePattern = Pattern.compile("^\\w+$");

    private FavoritesDataSource mFavoritesDatasource = Factory.resolve(FavoritesDataSource.class);

    private BoardsListAdapter mAdapter;

    @BindView(android.R.id.list)
    protected ListView mListView;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Observable<Boolean> updateBoardsListFromServer;

    @Inject
    protected OkHttpClient okHttpClient;

    @Inject
    protected ApplicationSettings mSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApplication.getComponent().inject(this);

        setHasOptionsMenu(true);

        mAdapter = new BoardsListAdapter(getContext());

        //Initially set boards list to the one we have previously stored in settings.
        updateBoardsUIList();

        requestBoardsListFromServer();

    }

    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pick_board_view, container, false);
        ButterKnife.bind(this, view);

        View pickBoardHeaderView = inflater.inflate(R.layout.pick_board_header, null);
        mListView.addHeaderView(pickBoardHeaderView);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            if (mListView.getItemAtPosition(i) instanceof BoardEntity) {
                BoardEntity item = (BoardEntity) mListView.getItemAtPosition(i);
                checkAndNavigateBoard(item.getCode());
            } else {
                Toast.makeText(getContext(), "Section!", Toast.LENGTH_SHORT).show();
            }

        });

        registerForContextMenu(mListView);


        //TODO move to navigation controller
        ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(getString(R.string.app_name));
        }


        final Button pickBoardButton = (Button) view.findViewById(R.id.pick_board_button);
        final EditText pickBoardInput = (EditText) view.findViewById(R.id.pick_board_input);


        pickBoardButton.setOnClickListener(v -> {
            String enteredBoard = pickBoardInput.getText().toString().trim();
            checkAndNavigateBoard(enteredBoard);
        });

        pickBoardInput.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String enteredBoard = pickBoardInput.getText().toString().trim();
                checkAndNavigateBoard(enteredBoard);
                return true;
            }
            return false;
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public void onRefresh() {
        requestBoardsListFromServer();
    }

    private void requestBoardsListFromServer() {
        Timber.v("requestBoardsListFromServer()");

        //Initialize observable that emits BoardModel list from server.
        if (updateBoardsListFromServer == null) {

            Request request = new Request.Builder()
                    .url(Websites.getDefault().getUrlBuilder().getBoardsUrl())
                    .build();

            updateBoardsListFromServer = Observable.fromCallable(() -> okHttpClient.newCall(request))
                    .subscribeOn(Schedulers.io())
                    .map(Call::execute)
                    .map(this::parseBoardsResponse)
                    .map(this::validateBoardsList)
                    .observeOn(AndroidSchedulers.mainThread()
                    );
        }

        Disposable d = updateBoardsListFromServer.subscribe(
                (success) -> updateBoardsUIList(),
                (error) -> Timber.e(error)
        );

        compositeDisposable.add(d);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BoardEntity item = (BoardEntity) mListView.getItemAtPosition(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));

        if (!mFavoritesDatasource.hasFavorites(Websites.getDefault().name(), item.getCode(), null)) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        BoardEntity model = (BoardEntity) mListView.getItemAtPosition(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = Websites.getDefault().getUrlBuilder().getPageUrlHtml(model.getCode(), 0);

                CompatibilityUtils.copyText(getActivity(), uri, uri);

                AppearanceUtils.showToastMessage(getActivity(), uri);
                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                addToFavorites(model.getCode());
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                removeFromFavorites(model);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.pickboard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_add_board_id:

                final EditTextDialog dialog = new EditTextDialog(getActivity());

                dialog.setTitle(getString(R.string.menu_add_favorites));
                dialog.setHint(getString(R.string.pick_board_input_hint));
                dialog.setPositiveButtonListener((d, which) -> {
                    String boardCode = dialog.getText();
                    boardCode = fixSlashes(boardCode);

                    if (validateBoardCode(boardCode)) {
                        addToFavorites(boardCode);
                    } else {
                        AppearanceUtils.showToastMessage(getActivity(), getString(R.string.warning_enter_board));
                    }
                });

                dialog.show();
                break;
            case R.id.refresh_menu_id:
                requestBoardsListFromServer();
                break;
        }

        return true;
    }

    private List<BoardModel> parseBoardsResponse(Response response) throws IOException {
        Timber.v("parseBoardsResponse()");

        if (!response.isSuccessful()) {
            response.close();
            throw new IOException("Server response cannot be proceeded");
        }

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ResponseBody responseBody = response.body();
        String responseString = "";

        if (responseBody != null) {
            responseString = responseBody.string();
        }
        JsonNode result = mapper.readValue(responseString, JsonNode.class);
        response.close();

        //Parse each category as single JsonNode
        Iterator iterator = result.getElements();
        ArrayList<BoardModel> models = new ArrayList<>();

        while (iterator.hasNext()) {
            JsonNode node = (JsonNode) iterator.next();
            MakabaBoardInfo[] data = mapper.convertValue(node, MakabaBoardInfo[].class);
            BoardModel[] tempBoardModels = MakabaModelsMapper.mapBoardModels(data);
            models.addAll(Arrays.asList(tempBoardModels));
        }
        return models;
    }

    private boolean validateBoardsList(List<BoardModel> boards) {
        Timber.v("validateBoardsList()");


        if (boards.isEmpty()) {
            Timber.e("Received empty boards list!");
        }

        //Check if received array differs from one that is currently stored in settings.
        if (!mSettings.getBoards().isEmpty() && GeneralUtils.equalLists(boards, mSettings.getBoards())) {
            Timber.d("Boards list has not been modified since last check");

        } else {
            Timber.d("Boards list has been modified, updating...");
            mSettings.setBoards((ArrayList<BoardModel>) boards);

        }
        return true;
    }

    private boolean updateBoardsUIList() {
        Timber.v("updateBoardsUIList()");

        List<BoardModel> boards = mSettings.getBoards();

        if (boards.isEmpty()) {
            return false;
        }

        mAdapter.clear();

        String[] categorySequenceToBeShown = new String[]{
                "Игры",
                "Политика",
                "Японская культура",
                "Разное",
                "Творчество",
                "Тематика",
                "Техника и софт",
                "Взрослым",
                "Пользовательские"
        };

        String currentCategory = null;

        for (String category : categorySequenceToBeShown) {
            List<String> visibleBoards = Arrays.asList(getResources().getStringArray(R.array.allowed_boards));

            for (BoardModel board : boards) {
                // ignore all boards except of matching category.
                if (!board.getCategory().equals(category)) {
                    continue;
                }
                //ignore invisible boards
                if (!visibleBoards.contains(board.getId()) && !mSettings.isDisplayAllBoards()) {
                    continue;
                }

                // add group header
                if (board.getCategory() != null && !board.getCategory().equals(currentCategory)) {
                    currentCategory = board.getCategory();
                    mAdapter.add(new SectionEntity(currentCategory));
                }
                // add item
                mAdapter.add(new BoardEntity(board.getId(), board.getName(), board.getBumpLimit()));
            }
        }

        // add favorite boards
        List<FavoritesEntity> favoriteBoards = mFavoritesDatasource.getFavoriteBoards();
        for (FavoritesEntity f : favoriteBoards) {
            String boardName = f.getBoard();
            mAdapter.addItemToFavoritesSection(boardName, findBoardByCode(boardName));
        }
        mAdapter.notifyDataSetChanged();
        return true;
    }

    private BoardModel findBoardByCode(String id) {
        for (BoardModel board : mSettings.getBoards()) {
            if (board.getId().equals(id)) {
                return board;
            }
        }
        return null;
    }

    private void checkAndNavigateBoard(String boardCode) {
        boardCode = fixSlashes(boardCode);
        boolean success = validateBoardCode(boardCode);
        if (!success) {
            AppearanceUtils.showToastMessage(getActivity(), getString(R.string.warning_enter_board));
        } else {
            NavigationService.getInstance().navigateBoard(Websites.getDefault().name(), boardCode);

        }
    }

    private void addToFavorites(String boardCode) {
        mFavoritesDatasource.addToFavorites(Websites.getDefault().name(), boardCode, null, null);
        mAdapter.addItemToFavoritesSection(boardCode, findBoardByCode(boardCode));
    }

    private void removeFromFavorites(BoardEntity model) {
        mFavoritesDatasource.removeFromFavorites(Websites.getDefault().name(), model.getCode(), null);
        mAdapter.removeItemFromFavoritesSection(model);
    }

    private boolean validateBoardCode(String boardCode) {
        return !StringUtils.isEmpty(boardCode) && boardCodePattern.matcher(boardCode).matches();

    }

    private String fixSlashes(String boardCode) {
        return boardCode.replaceAll("/", "").toLowerCase(Locale.US);
    }

}
