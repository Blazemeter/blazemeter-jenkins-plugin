package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.base.BZAObject;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONObject;

import java.io.IOException;

/**
 *
 */
public class WorkspaceExt extends Workspace {

    public WorkspaceExt(BlazeMeterUtils utils, String id, String name) {
        super(utils, id, name);
    }

    /**
     * Get workspace
     * GET request to 'https://a.blazemeter.com/api/v4/workspaces/{workspaceId}'
     * @param utils - BlazeMeterUtils that contains logging and http setup
     * @param id - workspaces Id
     * @return Workspace entity, which contains workspace ID and name (workspaces label)
     */
    public static Workspace getWorkspace(BlazeMeterUtils utils, String id) throws IOException {
        Logger logger = utils.getLogger();
        logger.info("Get Workspace id=" + id);
        String uri = utils.getAddress() + String.format("/api/v4/workspaces/%s", BZAObject.encode(logger, id));
        JSONObject response = utils.execute(utils.createGet(uri));
        return Workspace.fromJSON(utils, response.getJSONObject("result"));
    }
}
