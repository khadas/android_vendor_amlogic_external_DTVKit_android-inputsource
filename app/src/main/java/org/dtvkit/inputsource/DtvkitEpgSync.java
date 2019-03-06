package org.dtvkit.inputsource;

import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;

import org.dtvkit.companionlibrary.EpgSyncJobService;
import org.dtvkit.companionlibrary.model.Channel;
import org.dtvkit.companionlibrary.model.EventPeriod;
import org.dtvkit.companionlibrary.model.InternalProviderData;
import org.dtvkit.companionlibrary.model.Program;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DtvkitEpgSync extends EpgSyncJobService {
    private static final String TAG = "EpgSyncJobService";

    @Override
    public List<Channel> getChannels() {
        List<Channel> channels = new ArrayList<>();

        Log.i(TAG, "Get channels for epg sync");

        try {
            JSONObject obj = DtvkitGlueClient.getInstance().request("Dvb.getListOfServices", new JSONArray());

            Log.i(TAG, obj.toString());

            JSONArray services = obj.getJSONArray("data");

            for (int i = 0; i < services.length(); i++)
            {
                JSONObject service = services.getJSONObject(i);
                String uri = service.getString("uri");

                InternalProviderData data = new InternalProviderData();
                data.put("dvbUri", uri);

                channels.add(new Channel.Builder()
                        .setDisplayName(service.getString("name"))
                        .setDisplayNumber(String.format(Locale.ENGLISH, "%d", service.getInt("lcn")))
                        .setServiceType(service.getBoolean("radio") ? TvContract.Channels.SERVICE_TYPE_AUDIO :
                                TvContract.Channels.SERVICE_TYPE_AUDIO_VIDEO)
                        .setOriginalNetworkId(Integer.parseInt(uri.substring(6, 10), 16))
                        .setTransportStreamId(Integer.parseInt(uri.substring(11, 15), 16))
                        .setServiceId(Integer.parseInt(uri.substring(16, 20), 16))
                        .setInternalProviderData(data)
                        .build());
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return channels;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channel, long startMs, long endMs) {
        List<Program> programs = new ArrayList<>();

        try {
        String dvbUri = String.format("dvb://%04x.%04x.%04x", channel.getOriginalNetworkId(), channel.getTransportStreamId(), channel.getServiceId());

            Log.i(TAG, String.format("Get channel programs for epg sync. Uri %s, startMs %d, endMs %d",
                dvbUri, startMs, endMs));

            JSONArray args = new JSONArray();
            args.put(dvbUri); // uri
            JSONArray events = DtvkitGlueClient.getInstance().request("Dvb.getListOfEvents", args).getJSONArray("data");

            for (int i = 0; i < events.length(); i++)
            {
                JSONObject event = events.getJSONObject(i);

                InternalProviderData data = new InternalProviderData();
                data.put("dvbUri", dvbUri);

                programs.add(new Program.Builder()
                        .setChannelId(channel.getId())
                        .setTitle(event.getString("name"))
                        .setStartTimeUtcMillis(event.getLong("startutc") * 1000)
                        .setEndTimeUtcMillis(event.getLong("endutc") * 1000)
                        .setDescription(event.getString("description"))
                        .setCanonicalGenres(getGenres(event.getString("genre")))
                        .setInternalProviderData(data)
                        .build());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return programs;
    }

    @Override
    public List<EventPeriod> getListOfUpdatedEventPeriods()
    {
        List<EventPeriod> eventPeriods = new ArrayList<>();

        Log.i(TAG, "getListOfUpdatedEventPeriods");

        try {
            JSONArray periods = DtvkitGlueClient.getInstance().request("Dvb.getListOfUpdatedEventPeriods", new JSONArray()).getJSONArray("data");

            Log.i(TAG, periods.toString());

            for (int i = 0; i < periods.length(); i++)
            {
                JSONObject period = periods.getJSONObject(i);

                eventPeriods.add(new EventPeriod(period.getString("uri"),period.getLong("startutc"),period.getLong("endutc")));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return eventPeriods;
    }

    private String[] getGenres(String genre)
    {
        switch (genre)
        {
            case "movies":
                return new String[]{TvContract.Programs.Genres.MOVIES};
            case "news":
                return new String[]{TvContract.Programs.Genres.NEWS};
            case "entertainment":
                return new String[]{TvContract.Programs.Genres.ENTERTAINMENT};
            case "sport":
                return new String[]{TvContract.Programs.Genres.SPORTS};
            case "childrens":
                return new String[]{TvContract.Programs.Genres.FAMILY_KIDS};
            case "music":
                return new String[]{TvContract.Programs.Genres.MUSIC};
            case "arts":
                return new String[]{TvContract.Programs.Genres.ARTS};
            case "social":
                return new String[]{TvContract.Programs.Genres.LIFE_STYLE};
            case "education":
                return new String[]{TvContract.Programs.Genres.EDUCATION};
            case "leisure":
                return new String[]{TvContract.Programs.Genres.LIFE_STYLE};
            default:
                return new String[]{};
        }
    }
}