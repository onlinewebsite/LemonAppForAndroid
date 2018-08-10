package tk.twilightlemon.lemonapp.Fragments;
/**
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\  =  /O
 * ____/`---'\____
 * .'  \\|     |//  `.
 * /  \\|||  :  |||//  \
 * /  _||||| -:- |||||-  \
 * |   | \\\  -  /// |   |
 * | \_|  ''\---/''  |   |
 * \  .-\__  `-`  ___/-. /
 * ___`. .'  /--.--\  `. . __
 * ."" '<  `.___\_<|>_/___.'  >'"".
 * | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 * \  \ `-.   \_ __\ /__ _/   .-` /  /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * 佛祖保佑        永无BUG
 */

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twilightlemon.lemonapp.Adapters.GDListAdapter;
import tk.twilightlemon.lemonapp.Adapters.SingerAndRadioListAdapter;
import tk.twilightlemon.lemonapp.Adapters.TopItemsAdapter;
import tk.twilightlemon.lemonapp.Helpers.HttpHelper;
import tk.twilightlemon.lemonapp.Helpers.InfoHelper;
import tk.twilightlemon.lemonapp.Helpers.MusicLib;
import tk.twilightlemon.lemonapp.R;
import tk.twilightlemon.lemonapp.Helpers.Settings;
import tk.twilightlemon.lemonapp.layouts.Adaptivelayout;
import tk.twilightlemon.lemonapp.layouts.MainActivity;
import tk.twilightlemon.lemonapp.layouts.MusicListPage;

//layout/second_fragment_layout.xml的交互逻辑
public class SecondFragment extends Fragment {
    private ListView Top_list;

    public static Fragment newInstance() {
        SecondFragment fragment = new SecondFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.second_fragment_layout, null);
        Top_list = view.findViewById(R.id.Top_list);
        LoadTopList(Top_list, 2);
        LoadFLGDList((ListView) view.findViewById(R.id.FLGD_list));
        LoadSingerList((ListView) view.findViewById(R.id.Singer_list));
        LoadRadioList((ListView) view.findViewById(R.id.Radio_list));
        LoadSearchTab(view);
        LoadMoreBtns(view);
        return view;
    }

    public void LoadSearchTab(View view) {
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText qq = new EditText(getContext());
                qq.setHint("搜索");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("搜索").setView(qq)
                        .setNegativeButton("取消", null);
                builder.setPositiveButton("搜索", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        MusicLib.Search(getActivity(), qq.getText().toString());
                    }
                });
                builder.show();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    ArrayList<String> Top_idList = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    public void LoadTopList(final ListView lv, final int forni) {
        HttpHelper.GetWeb(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String jsondata = "{\"data\":" + msg.obj.toString().replace("jsonCallback(", "").replace("}]\n)", "") + "}]" + "}";
                    JSONObject o = new JSONObject(jsondata);
                    ArrayList<InfoHelper.MusicTop> data = new ArrayList<>();
                    Top_idList.clear();
                    int igne = forni;
                    for (int dat = 0; dat < o.getJSONArray("data").length(); ++dat) {
                        for (int i = 0; i < o.getJSONArray("data").getJSONObject(dat).getJSONArray("List").length(); ++i) {
                            JSONObject json = o.getJSONArray("data").getJSONObject(dat).getJSONArray("List").getJSONObject(i);
                            InfoHelper.MusicTop dt = new InfoHelper().new MusicTop();
                            dt.Name = json.getString("ListName");
                            if (dt.Name.contains("MV"))//排除MV榜
                                continue;
                            dt.ID = json.getString("topID");
                            dt.Photo = json.getString("pic_v12");
                            data.add(dt);
                            Top_idList.add(dt.ID);
                            if (igne != -1 && i == igne)
                                break;
                        }
                        if (igne != -1)
                            break;
                    }
                    TopItemsAdapter tia = new TopItemsAdapter(getActivity(), data);
                    lv.setAdapter(tia);
                    FirstFragment.setListViewHeightBasedOnChildren(lv);
                } catch (Exception e) {
                }
            }
        }, "https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_opt.fcg?page=index&format=html&tpl=macv4&v8debug=1", null);
        if (forni != -1) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    InfoHelper.MusicGData dt = new InfoHelper().new MusicGData();
                    InfoHelper.MusicTop dat = ((TopItemsAdapter) lv.getAdapter()).getMdata().get(Top_idList.get(i));
                    dt.Data = dat.Data;
                    dt.name = dat.Name;
                    Settings.ListData = dt;
                    Intent intent = new Intent(getActivity(), MusicListPage.class);
                    getActivity().startActivityForResult(intent, 1000);
                }
            });
        }
    }

    ArrayList<InfoHelper.MusicGData> FLGDdata = new ArrayList<>();
    ArrayList<InfoHelper.MusicGData> FLGDdat = new ArrayList<>();

    public void LoadFLGDList(final ListView lv) {
        MusicLib.GetFLGDItems("10000000", new Handler() {
            @Override
            public void handleMessage(Message msg) {
                FLGDdata = (ArrayList<InfoHelper.MusicGData>) msg.obj;
                GDListAdapter ga = new GDListAdapter(getActivity(), FLGDdata);
                lv.setAdapter(ga);
                FirstFragment.setListViewHeightBasedOnChildren(lv);
            }
        }, 4);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                MusicLib.GetGDbyID(FLGDdata.get(position), getActivity());
            }
        });
    }

    boolean Top_moreindex = false;

    public void LoadMoreBtns(View view) {
        view.findViewById(R.id.Top_moreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.Loading(view);
                if (Top_moreindex) LoadTopList(Top_list, 2);
                else LoadTopList(Top_list, -1);
                Top_moreindex = !Top_moreindex;
            }
        });
        view.findViewById(R.id.FLGD_moreBtn).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View view) {
                final HashMap<String, String> data = new HashMap<String, String>();
                data.put("Connection", "keep-alive");
                data.put("CacheControl", "max-age=0");
                data.put("Upgrade", "1");
                data.put("UserAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.110 Safari/537.36");
                data.put("Accept", "*/*");
                data.put("Referer", "https://y.qq.com/portal/player.html");
                data.put("Host", "c.y.qq.com");
                data.put("AcceptLanguage", "zh-CN,zh;q=0.8");
                data.put("Cookie", "pgv_pvi=1693112320; RK=DKOGai2+wu; pgv_pvid=1804673584; ptcz=3a23e0a915ddf05c5addbede97812033b60be2a192f7c3ecb41aa0d60912ff26; pgv_si=s4366031872; _qpsvr_localtk=0.3782697029073365; ptisp=ctc; luin=o2728578956; lskey=00010000863c7a430b79e2cf0263ff24a1e97b0694ad14fcee720a1dc16ccba0717d728d32fcadda6c1109ff; pt2gguin=o2728578956; uin=o2728578956; skey=@PjlklcXgw; p_uin=o2728578956; p_skey=ROnI4JEkWgKYtgppi3CnVTETY3aHAIes-2eDPfGQcVg_; pt4_token=wC-2b7WFwI*8aKZBjbBb7f4Am4rskj11MmN7bvuacJQ_; p_luin=o2728578956; p_lskey=00040000e56d131f47948fb5a2bec49de6174d7938c2eb45cb224af316b053543412fd9393f83ee26a451e15; ts_refer=ui.ptlogin2.qq.com/cgi-bin/login; ts_last=y.qq.com/n/yqq/playlist/2591355982.html; ts_uid=1420532256; yqq_stat=0");
                HttpHelper.GetWeb(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        try {
                            JSONArray o = new JSONObject(msg.obj.toString()).getJSONObject("data").getJSONArray("categories");
                            final InfoHelper.MusicFLGDIndexItemsList data = new InfoHelper().new MusicFLGDIndexItemsList();
                            final ArrayList<String> idList = new ArrayList<>();
                            for (int csr = 0; csr < o.length(); ++csr) {
                                for (int i = 0; i < o.getJSONObject(csr).getJSONArray("items").length(); ++i) {//lauch
                                    JSONObject js = o.getJSONObject(csr).getJSONArray("items").getJSONObject(i);
                                    InfoHelper.MusicFLGDIndexItems ite = new InfoHelper().new MusicFLGDIndexItems();
                                    ite.id = js.getString("categoryId");
                                    ite.name = js.getString("categoryName");
                                    data.Data.add(ite);
                                    idList.add(ite.name);
                                }
                            }
                            MusicLib.GetFLGDItems("10000000", new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    final InfoHelper.AdaptiveData aData = new InfoHelper().new AdaptiveData();
                                    aData.ChooseData.add(idList.toArray(new String[idList.size()]));
                                    aData.title = "分类歌单";
                                    aData.ChooseCallBack.add(new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            final ListView lv = (ListView) msg.obj;
                                            MusicLib.GetFLGDItems(data.Data.get(msg.what).id, new Handler() {
                                                @Override
                                                public void handleMessage(Message msg) {
                                                    FLGDdat = (ArrayList<InfoHelper.MusicGData>) msg.obj;
                                                    GDListAdapter ga = new GDListAdapter(getActivity(), FLGDdat);
                                                    lv.setAdapter(ga);
                                                    FirstFragment.setListViewHeightBasedOnChildren(lv);
                                                }
                                            }, -1);
                                        }
                                    });
                                    aData.ListOnClick = new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                            MusicLib.GetGDbyID(FLGDdat.get(i), getActivity());
                                        }
                                    };
                                    FLGDdat = (ArrayList<InfoHelper.MusicGData>) msg.obj;
                                    GDListAdapter ga = new GDListAdapter(getActivity(), FLGDdat);
                                    aData.CSData = ga;
                                    Settings.AdapData = aData;
                                    Intent intent = new Intent(getActivity(), Adaptivelayout.class);
                                    getActivity().startActivityForResult(intent, 1000);
                                }
                            }, -1);
                        } catch (Exception e) {
                        }
                    }
                }, "https://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_tag_conf.fcg?g_tk=1206122277&loginUin=" + Settings.qq + "&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0", data);
            }
        });
        view.findViewById(R.id.Singer_moreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicLib.GetSingerByTag("all_all_all", new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        SingerData = (ArrayList<InfoHelper.SingerAndRadioData>) msg.obj;
                        SingerAndRadioListAdapter sla = new SingerAndRadioListAdapter(getActivity(), SingerData);
                        InfoHelper.AdaptiveData aData = new InfoHelper().new AdaptiveData();
                        aData.CSData = sla;
                        aData.title = "歌手";
                        final String[] cData = new String[]{"热门", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
                        aData.ChooseData.add(new String[]{"全部", "华语男", "华语女", "华语组合", "韩国男", "韩国女", "韩国组合", "日本男", "日本女", "日本组合", "欧美男", "欧美女", "欧美组合", "乐团", "演奏家", "作曲家", "指挥家", "其他"});
                        aData.ChooseData.add(cData);
                        final String[] zData = new String[]{"all_all_", "cn_man_", "cn_woman_", "cn_team_", "k_man_", "k_woman_", "k_team_", "j_man_", "j_woman_", "j_team_", "eu_man_", "eu_woman_", "eu_team_", "c_orchestra_", "c_performer_", "c_composer_", "c_cantor_", "other_other_"};
                        aData.ChooseCallBack.add(new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                final ListView lv = (ListView) msg.obj;
                                SingerKey1 = zData[msg.what];
                                MusicLib.GetSingerByTag(SingerKey1 + SingerKey2, new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        SingerData.clear();
                                        SingerData = (ArrayList<InfoHelper.SingerAndRadioData>) msg.obj;
                                        lv.setAdapter(new SingerAndRadioListAdapter(getActivity(), SingerData));
                                        FirstFragment.setListViewHeightBasedOnChildren(lv);
                                    }
                                }, 50);
                            }
                        });
                        aData.ChooseCallBack.add(new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                final ListView lv = (ListView) msg.obj;
                                SingerKey2 = cData[msg.what].replace("热门", "all").replace("#", "9");
                                MusicLib.GetSingerByTag(SingerKey1 + SingerKey2, new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        SingerData.clear();
                                        SingerData = (ArrayList<InfoHelper.SingerAndRadioData>) msg.obj;
                                        lv.setAdapter(new SingerAndRadioListAdapter(getActivity(), SingerData));
                                        FirstFragment.setListViewHeightBasedOnChildren(lv);
                                    }
                                }, 50);
                            }
                        });
                        aData.ListOnClick = new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                MusicLib.Search(getActivity(), SingerData.get(i).name);
                            }
                        };
                        Settings.AdapData = aData;
                        Intent intent = new Intent(getActivity(), Adaptivelayout.class);
                        getActivity().startActivityForResult(intent, 1000);
                    }
                }, -1);
            }
        });
        view.findViewById(R.id.Radio_moreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicLib.GetRadioListByTag(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        final ArrayList<ArrayList<InfoHelper.SingerAndRadioData>> data = (ArrayList<ArrayList<InfoHelper.SingerAndRadioData>>) msg.obj;
                        InfoHelper.AdaptiveData aData = new InfoHelper().new AdaptiveData();
                        aData.ChooseData.add(new String[]{"热门", "下午", "情感", "主题", "场景", "曲风", "语言", "人群", "乐器", "地区"});
                        aData.title = "电台";
                        RadioMData = data.get(0);
                        aData.CSData = new SingerAndRadioListAdapter(getActivity(), RadioMData);
                        aData.ChooseCallBack.add(new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                ListView lv = (ListView) msg.obj;
                                RadioMData = data.get(msg.what);
                                lv.setAdapter(new SingerAndRadioListAdapter(getActivity(), RadioMData));
                            }
                        });
                        aData.ListOnClick = new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                MusicLib.GetRadioMusicById(RadioMData.get(i).id, new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        InfoHelper.MusicGData GData = new InfoHelper().new MusicGData();
                                        GData.Data.add((InfoHelper.Music) msg.obj);
                                        GData.name="Radio";
                                        GData.id=RadioMData.get(i).id;
                                        Settings.ListData = GData;
                                        Message message = new Message();
                                        message.what = 0;
                                        message.obj = 0;
                                        Settings.Callback_PlayMusic.sendMessage(message);
                                        Settings.Callback_Close.sendMessage(new Message());
                                    }
                                });
                            }
                        };
                        Settings.AdapData = aData;
                        Intent intent = new Intent(getActivity(), Adaptivelayout.class);
                        getActivity().startActivityForResult(intent, 1000);
                    }
                }, -1);
            }
        });
    }

    String SingerKey1 = "all_all_";
    String SingerKey2 = "all";
    ArrayList<InfoHelper.SingerAndRadioData> SingerData;

    public void LoadSingerList(final ListView lv) {
        MusicLib.GetSingerByTag("all_all_all", new Handler() {
            @Override
            public void handleMessage(Message msg) {
                lv.setAdapter(new SingerAndRadioListAdapter(getActivity(), (ArrayList<InfoHelper.SingerAndRadioData>) msg.obj));
                FirstFragment.setListViewHeightBasedOnChildren(lv);
            }
        }, 4);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                MusicLib.GetSingerByTag("all_all_all", new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        MusicLib.Search(getActivity(), ((ArrayList<InfoHelper.SingerAndRadioData>) msg.obj).get(i).name);
                    }
                }, 4);
            }
        });
    }

    ArrayList<InfoHelper.SingerAndRadioData> RadioMData = new ArrayList<>();

    public void LoadRadioList(final ListView lv) {
        MusicLib.GetRadioListByTag(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ArrayList<ArrayList<InfoHelper.SingerAndRadioData>> data = (ArrayList<ArrayList<InfoHelper.SingerAndRadioData>>) msg.obj;
                lv.setAdapter(new SingerAndRadioListAdapter(getActivity(), data.get(0)));
                FirstFragment.setListViewHeightBasedOnChildren(lv);
            }
        }, 4);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                MusicLib.GetRadioListByTag(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        final ArrayList<ArrayList<InfoHelper.SingerAndRadioData>> data = (ArrayList<ArrayList<InfoHelper.SingerAndRadioData>>) msg.obj;
                        MusicLib.GetRadioMusicById(data.get(0).get(i).id, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                InfoHelper.MusicGData GData = new InfoHelper().new MusicGData();
                                GData.Data.add((InfoHelper.Music) msg.obj);
                                GData.name="Radio";
                                GData.id=data.get(0).get(i).id;
                                Settings.ListData = GData;
                                Message message = new Message();
                                message.what = 0;
                                message.obj = 0;
                                Settings.Callback_PlayMusic.sendMessage(message);
                            }
                        });
                    }
                }, 4);
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}