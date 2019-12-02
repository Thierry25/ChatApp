package marcelin.thierry.chatapp.classes;

import android.app.Activity;
import androidx.core.app.ActivityCompat;

import com.coderconsole.cextracter.common.permissions.PermissionWrapper;

import java.util.ArrayList;
import java.util.List;

public class RunTimePermissionWrapper extends PermissionWrapper {


    /**
     * Handle the Runtime Permission
     *
     * @param activity
     * @param multiplePermissionRequestCode Request Code for Permission
     * @param multiplePermissions           varArgs of multiple Permissions
     */
    public static void handleRunTimePermission(Activity activity, int multiplePermissionRequestCode, String... multiplePermissions) {

        List<String> neededPermissionList = getDeniedPermissionList(activity, multiplePermissions);

        if (neededPermissionList == null) {
            return;
        }

        int permissionSize = neededPermissionList.size();
        if (permissionSize > 0) {
            ActivityCompat.requestPermissions(activity, neededPermissionList.toArray(new String[permissionSize]), multiplePermissionRequestCode);
        }

    }


    public static List<String> getDeniedPermissionList(Activity context, String[] multiplePermissions) {

        if (context == null || multiplePermissions == null)
            return null;

        List<String> neededPermissionList = new ArrayList<>();

        for (String permission : multiplePermissions) {
            if (!hasPermissions(context, permission)) {
                neededPermissionList.add(permission);
            }
        }
        return neededPermissionList;
    }

    public static boolean isAllPermissionGranted(Activity activity, String[] multiplePermissions) {
        List<String> list = getDeniedPermissionList(activity, multiplePermissions);
        return list != null && list.size() == 0;
    }


    public interface REQUEST_CODE {
        int MULTIPLE_WALKTHROUGH = 200;
    }
}
