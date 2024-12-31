package ren.rymc.residenceform.form;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.SimpleForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import ren.rymc.residenceform.ResidenceForm;
import ren.rymc.residenceform.utils.Facing;
import ren.rymc.residenceform.utils.TempSelection;
import ren.rymc.residenceform.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainForm {
    public static void sendMainResidenceForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("领地菜单")
                        .content("§6 暗之光服务器领地菜单")
                        .button("领地传送")
                        .button("领地管理")
                        .button("领地创建")
                        .button("领地工具")
                        .button("回主菜单")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) sendResTeleportForm(player);
                                else if (id == 1) sendResSettingForm(player);
                                else if (id == 2) sendResCreateSelectForm(player);
                                else if (id == 3) sendResToolsForm(player);
                                else if (id == 4) Bukkit.dispatchCommand(player, "forms open main-form");
                            }
                        })
        );
    }

    public static void sendResCreateSelectForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        PermissionGroup group = Residence.getInstance().getPlayerManager().getResidencePlayer(player).getGroup();
        SelectionManager.Selection selection = Residence.getInstance().getSelectionManager().getSelection(player);
        String content;
        Location loc1 = selection.getBaseLoc1();
        Location loc2 = selection.getBaseLoc2();
        if (loc1 == null || loc2 == null) content = "未创建选区\n\n";
        else content =  "顶点坐标1: " + Utils.blockLocToString(loc1) + "\n" +
                        "顶点坐标2: " + Utils.blockLocToString(loc2) + "\n" +
                        "所在维度: " + (loc1.getWorld() == null ? "未知" : loc1.getWorld().getName()) + "\n" +
                        "长: " + selection.getBaseArea().getXSize() + "\n" +
                        "宽: " + selection.getBaseArea().getZSize() + "\n" +
                        "高: " + selection.getBaseArea().getYSize() + "\n" +
                        "价格: " + selection.getBaseArea().getCost(group) + "美元\n\n";
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("领地创建菜单")
                        .content("§a请使用选区工具（木棍）圈地。§e不懂的话可查阅下方帮助。\n§c以下功能若不理解含义请勿使用§r\n\n当前选区:\n" + content)
                        .button("圈地选区帮助")
                        .button("创建领地\n§3圈好区以后再点我")
                        .button("以你为中心选区")
                        .button("开启/关闭自动选区")
                        .button("扩大/缩小选区")
                        .button("手动输入选区坐标")
                        .button("将此选区保存为草稿")
                        .button("导入领地草稿")
                        .button("删除领地草稿")
                        .button("返回领地主菜单")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) sendSelectHelpForm(player);
                                else if (id == 1) sendResCreateForm(player);
                                else if (id == 2) sendResPlayerSelectForm(player);
                                else if (id == 3) Bukkit.dispatchCommand(player, "res select auto");
                                else if (id == 4) sendResSelectExpandAndContractForm(player);
                                else if (id == 5) Bukkit.dispatchCommand(player, "forms open selectdeny");
                                else if (id == 6) sendResTempSelectionForm(player);
                                else if (id == 7) sendResTempSelectionImportForm(player);
                                else if (id == 8) sendResTempSelectionRemoveForm(player);
                                else if (id == 9) sendMainResidenceForm(player);
                            }
                        })
        );
    }

    public static void sendSelectHelpForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("圈地帮助")
                        .content("使用木棍圈地：手持木棍右键(或点按屏幕)选择一个点，左键(或长按屏幕)选择第二个点，成功选上后会有消息提示。选出来的两个点的连线就是领地范围的体对角线。选择成功后在聊天栏输入命令“/res create <你给领地起的名字>”或再次打开领地创建菜单点击“创建领地”完成创建。\n§e注意！领地范围是一个长方体，你用木棍选出来的连线的是长方体的体对角线，不是长方形的对角线，它是立体的，千万不要只圈一层地表皮！东西来之不易，不要轻易被别人偷走！")
                        .button("返回上一级")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) MainForm.sendResCreateSelectForm(player);
                            }
                        })
        );
    }


    public static void sendResCreateForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        PermissionGroup group = Residence.getInstance().getPlayerManager().getResidencePlayer(player).getGroup();
        CuboidArea baseArea = Residence.getInstance().getSelectionManager().getSelection(player).getBaseArea();
        if (baseArea == null) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地创建")
                        .input("此次创建将花费: $" + baseArea.getCost(group) + " \n\n请给你要创建的领地起个名字", "支持中英文,数字,下划线和连字符")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(0);
                                if (input != null && !input.trim().equals("")) {
                                    Bukkit.dispatchCommand(player, "res create " + input);
                                    Residence.getInstance().getSelectionManager().clearSelection(player);
                                }
                            }
                        })
        );
    }

    public static void sendResTempSelectionRemoveForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        String[] tempResList = TempSelection.getPlayerTempSelectionNameList(player);
        tempResList[0] = "请选择你要删除的领地草稿";
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地草稿删除")
                        .dropdown("领地草稿列表", tempResList)
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                if (response.getDropdown(0) != 0) {
                                    TempSelection tempSelection = TempSelection.getTempSelection(player, tempResList[response.getDropdown(0)]);
                                    if (tempSelection != null) TempSelection.removeTempSelection(player, tempSelection.getName());
                                }
                                sendResCreateSelectForm(player);
                            }
                        })
        );
    }

    public static void sendResTempSelectionImportForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        String[] tempResList = TempSelection.getPlayerTempSelectionNameList(player);
        tempResList[0] = "请选择你要导入的领地草稿";
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地草稿导入")
                        .dropdown("领地草稿列表", tempResList)
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            check: if (response.isCorrect()) {
                                if (response.getDropdown(0) != 0) {
                                    TempSelection tempSelection = TempSelection.getTempSelection(player, tempResList[response.getDropdown(0)]);
                                    if (tempSelection == null) break check;
                                    SelectionManager.Selection selection = Residence.getInstance().getSelectionManager().getSelection(player);
                                    Location loc1 = tempSelection.getLoc1();
                                    Location loc2 = tempSelection.getLoc2();
                                    if (loc1 == null || loc2 == null) break check;
                                    selection.setBaseLoc1(loc1);
                                    selection.setBaseLoc2(loc2);
                                }
                            }
                            sendResCreateSelectForm(player);
                        })
        );
    }

    public static void sendResTempSelectionForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地选区草稿保存")
                        .input("领地草稿会在服务器重启后消失\n一个账号最多保存5个\n\n请为此草稿设置一个名称", "不能为空")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(0);
                                if (input != null && !input.trim().equals("")) {
                                    SelectionManager.Selection selection = Residence.getInstance().getSelectionManager().getSelection(player);
                                    TempSelection tempSelection = new TempSelection(player, input, selection.getBaseLoc1(), selection.getBaseLoc2());
                                    TempSelection.addTempSelection(tempSelection);
                                    Residence.getInstance().getSelectionManager().clearSelection(player);
                                }
                                sendResCreateSelectForm(player);
                            }
                        })
        );
    }

    public static void sendResSelectExpandAndContractForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地选区扩展/缩小")
                        .dropdown("你当前面对的方向: " + Facing.facing(player.getLocation().getYaw()).getName() + "\n\n扩展/缩小的方向", Facing.facingList())
                        .input("范围", "数字,不填则返回上一级菜单")
                        .toggle("模式(关闭为扩大,开启为缩小)")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(1);
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ")) {
                                    String command = response.getToggle(2) ? "contract" : "expand";
                                    Location location = player.getLocation();
                                    player.teleport(Facing.translateLocation(location, Facing.facing(response.getDropdown(0))));
                                    Bukkit.dispatchCommand(player, "res select " + command + " " + input.trim());
                                    player.teleport(location);
                                }
                                sendResCreateSelectForm(player);
                            }
                        })
        );
    }

    public static void sendResManualSelectForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8手动输入坐标")
                        .input("顶点坐标1(格式: X,Y,Z)", "坐标值，注意使用英文逗号")
                        .input("顶点坐标2(格式: X,Y,Z)", "坐标值，注意使用英文逗号")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            check: if (response.isCorrect()) {
                                String input1 = response.getInput(0);
                                String input2 = response.getInput(1);
                                if (input1 == null || input2 == null) break check;
                                Location loc1 = Utils.stringToBlockLoc(input1, player.getWorld());
                                Location loc2 = Utils.stringToBlockLoc(input2, player.getWorld());
                                if (loc1 == null || loc2 == null) break check;
                                SelectionManager sm = Residence.getInstance().getSelectionManager();
                                sm.clearSelection(player);
                                sm.getSelection(player).setBaseLoc1(loc1);
                                sm.getSelection(player).setBaseLoc2(loc2);
                            }
                            sendResCreateSelectForm(player);
                        })
        );
    }

    public static void sendResPlayerSelectForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8以你为中心创建领地")
                        .input("长", "数字(整数)")
                        .input("宽", "数字(整数)")
                        .input("高", "数字(整数)")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String X = response.getInput(0);
                                String Y = response.getInput(2);
                                String Z = response.getInput(1);
                                Bukkit.dispatchCommand(player, "res select " + X + " " + Y + " " + Z);
                                sendResCreateSelectForm(player);
                            }
                        })
        );
    }

    public static void sendResToolsForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("领地工具")
                        .content("§7暗之光服务器领地菜单")
                        .button("查看当前领地边界")
                        .button("领地信息查询")
                        .button("插件信息查看")
                        .button("返回领地主菜单")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) Bukkit.dispatchCommand(player, "res show");
                                else if (id == 1) sendResInfoForm(player);
                                else if (id == 2) sendPluginInfoForm(player);
                                else if (id == 3) sendMainResidenceForm(player);
                            }
                        })
        );
    }

    public static void sendResInfoForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        HashMap<String, ClaimedResidence> residenceList = Utils.getNormalResidenceList(player);
        String[] resList = new String[residenceList.size() + 1];
        int i = 1;
        resList[0] = "选择领地或使用下方输入框";
        for (Map.Entry<String, ClaimedResidence> entry : residenceList.entrySet()) {
            resList[i++] = entry.getKey();
        }
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地信息查询")
                        .dropdown("你可以使用此下拉框", resList)
                        .input("或者在下方输入名称", "完整领地名称")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(1);
                                String residence = null;
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ")) {
                                    residence = input.trim();
                                } else if (response.getDropdown(0) != 0) {
                                    residence = resList[response.getDropdown(0)];
                                }
                                if (residence == null) {
                                    sendMainResidenceForm(player);
                                } else {
                                    Bukkit.dispatchCommand(player, "res info " + residence);
                                }
                            }
                        })
        );
    }

    public static void sendPluginInfoForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("插件信息")
                        .content("§7暗之光服务器基岩版领地菜单\n基于RENaa_FD的ResidenceForm本地化而来\n源代码使用 MIT License\n修改: GorgeousStar（服主）\n\n\n§a===============================\n§d         暗之光服务器\n§a===============================\n\n\n\n\n")
                        .button("返回")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) MainForm.sendMainResidenceForm(player);
                            }
                        })
        );
    }

    public static void sendResTeleportForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        HashMap<String, ClaimedResidence> residenceList = Utils.getNormalResidenceList(player);
        String[] resList = new String[residenceList.size() + 1];
        int i = 1;
        resList[0] = "选择领地或使用下方输入框";
        for (Map.Entry<String, ClaimedResidence> entry : residenceList.entrySet()) {
            resList[i++] = entry.getKey();
        }
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地传送")
                        .dropdown("你可以使用此下拉框(§c只会显示你创建或加入的§r)", resList)
                        .input("其它领地请在下方输入名称", "领地名称")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(1);
                                String residence = null;
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ")) {
                                    residence = input.trim();
                                } else if (response.getDropdown(0) != 0) {
                                    residence = resList[response.getDropdown(0)];
                                }
                                if (residence == null) {
                                    sendMainResidenceForm(player);
                                } else {
                                    Bukkit.dispatchCommand(player, "res tp " + residence);
                                }
                            }
                        })
        );
    }

    public static void sendResSettingForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("领地管理菜单")
                        .content("当前管理的领地: " + residence.getName())
                        .button("公共权限查看与修改")
                        .button("特定玩家权限设置")
                        .button("信任玩家(成员)管理")
                        .button("领地传送点设置")
                        .button("踢出领地内玩家")
                        .button("敏感操作")
                        .button("返回领地选择")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) sendResSetForm(player, residence);
                                else if (id == 1) sendResPSetForm(player, residence);
                                else if (id == 2) sendResTrustedPlayerSettingForm(player, residence);
                                else if (id == 3) sendResTpSetForm(player, residence);
                                else if (id == 4) sendResKickForm(player, residence);
                                else if (id == 5) sendResSensitiveOperationForm(player, residence);
                                else if (id == 6) sendResSettingForm(player);
                            }
                        })
        );
    }

    public static void sendResSensitiveOperationForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!residence.isOwner(uuid) && !player.isOp()) {
            SimpleForm.Builder builder = SimpleForm.builder().title("领地菜单").content("你没有权限\n\n").button("返回")
                    .responseHandler((f, r) -> {
                        if (f.parseResponse(r).isCorrect()) sendResSettingForm(player, residence);
                    });
            FloodgateApi.getInstance().getPlayer(uuid).sendForm(builder);
            return;
        }
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("领地管理菜单")
                        .content("当前管理领地: " + residence.getName())
                        .button("重命名领地")
                        .button("扩大/缩小领地")
                        .button("转让领地")
                        .button("删除领地")
                        .button("返回领地管理")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) sendResRenameForm(player, residence);
                                else if (id == 1) sendResExtendAndContractForm(player, residence);
                                else if (id == 2) sendResGiveForm(player, residence);
                                else if (id == 3) sendResRemoveForm(player, residence);
                                else if (id == 4) sendResSettingForm(player, residence);
                            }
                        })
        );
    }

    public static void sendResRemoveForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!residence.isOwner(uuid) && !player.isOp()) return;
        FloodgatePlayer fgPlayer = FloodgateApi.getInstance().getPlayer(uuid);
        fgPlayer.sendForm(
                CustomForm.builder()
                        .title("§8领地 §l" + residence.getName() + " §r§8删除")
                        .input("请输入领地名以确认", "完整领地名(含大小写)")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(0);
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ") && input.trim().equals(residence.getName())) {
                                    fgPlayer.sendForm(
                                            SimpleForm.builder()
                                                    .title("§8领地 §l" + residence.getName() + " §r§8删除")
                                                    .content("§c你确定?此操作不可撤回!!\n\n")
                                                    .button("确定")
                                                    .button("再想想")
                                                    .responseHandler((f1, r1) -> {
                                                        SimpleFormResponse response1 = f1.parseResponse(r1);
                                                        if (response1.isCorrect()) {
                                                            int id = response1.getClickedButtonId();
                                                            if (id == 0)
                                                                Residence.getInstance().getResidenceManager().removeResidence(residence);
                                                            else if (id == 1)
                                                                sendResSensitiveOperationForm(player, residence);
                                                        }
                                                    })
                                    );
                                    return;
                                }
                                sendResSensitiveOperationForm(player, residence);
                            }
                        })
        );
    }

    public static void sendResGiveForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!residence.isOwner(uuid) && !player.isOp()) return;
        FloodgatePlayer fgPlayer = FloodgateApi.getInstance().getPlayer(uuid);
        fgPlayer.sendForm(
                CustomForm.builder()
                        .title("§8领地 §l" + residence.getName() + " §r§8转让")
                        .input("请输入玩家名", "完整玩家名(含大小写)")
                        .input("请再次输入玩家名以确认", "请再次输入玩家名")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(0);
                                String input1 = response.getInput(1);
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ") && input1 != null && input.trim().equals(input1.trim())) {
                                    fgPlayer.sendForm(
                                            SimpleForm.builder()
                                                    .title("§8领地 §l" + residence.getName() + " §r§8转让")
                                                    .content("你确定?此操作不可撤回!\n\n")
                                                    .button("确定")
                                                    .button("再想想")
                                                    .responseHandler((f1, r1) -> {
                                                        SimpleFormResponse response1 = f1.parseResponse(r1);
                                                        if (response1.isCorrect()) {
                                                            int id = response1.getClickedButtonId();
                                                            if (id == 0)
                                                                Residence.getInstance().getResidenceManager().giveResidence(player, input.trim(), residence, false, false);
                                                            if (id == 1)
                                                                sendResSensitiveOperationForm(player, residence);
                                                        }
                                                    })
                                    );
                                    return;
                                }
                                sendResSensitiveOperationForm(player, residence);
                            }
                        })
        );
    }

    public static void sendResRenameForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!residence.isOwner(uuid) && !player.isOp()) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地 §l" + residence.getName() + " §r§8重命名")
                        .input("请输入新的领地名称", "支持汉字,大小写字母,数字,下划线和连字符")
                        .input("请再次输入以确认领地名称", "请再次输入领地名称")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(0);
                                String input1 = response.getInput(1);
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ") && input1 != null && input.trim().equals(input1.trim())) {
                                    Residence.getInstance().getResidenceManager().renameResidence(player, residence.getName(), input.trim(), false);
                                }
                                sendResSensitiveOperationForm(player, residence);
                            }
                        })
        );
    }

    public static void sendResKickForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!Utils.hasManagePermission(player, residence) && !player.isOp()) return;
        String[] playerNameList = Utils.getPlayersInResidence(residence);
        playerNameList[0] = "请选择玩家";
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8踢出领地内玩家")
                        .dropdown("领地内玩家列表", playerNameList)
                        .input("或使用下方输入框", "完整玩家名称")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(1);
                                String targetPlayer = null;
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ")) {
                                    targetPlayer = input;
                                } else if (response.getDropdown(0) != 0) {
                                    targetPlayer = playerNameList[response.getDropdown(0)];
                                }
                                Utils.kickPlayer(targetPlayer, residence);
                                sendResSettingForm(player, residence);
                            }
                        })
        );
    }

    public static void sendResTpSetForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!Utils.hasManagePermission(player, residence) && !player.isOp()) return;
        Location location = player.getLocation();
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("领地传送点设置")
                        .content("将你所在的位置设置为领地传送点\n§e（其它人通过菜单或命令传送至此领地时会去往的坐标）§r\n领地: " + residence.getName() + "\n你的坐标:" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "\n\n")
                        .button("将传送点设置为当前坐标\n§3(想让别人传送过来的话还要打开tp权限)")
                        .button("返回领地管理")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) residence.setTpLoc(player, false);
                                sendResSettingForm(player, residence);
                            }
                        })
        );
    }

    public static void sendResExtendAndContractForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!residence.isOwner(uuid) && !player.isOp()) return;
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地 §l" + residence.getName() + " §r§8扩展/缩小")
                        .dropdown("你当前面对的方向: " + Facing.facing(player.getLocation().getYaw()).getName() + "\n\n扩展/缩小的方向", Facing.facingList())
                        .input("扩展范围", "数字,不填则返回上一级菜单")
                        .toggle("模式(关闭为扩大,开启为缩小)")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(1);
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ")) {
                                    String command = response.getToggle(2) ? "contract" : "expand";
                                    Location location = player.getLocation();
                                    player.teleport(Facing.translateLocation(location, Facing.facing(response.getDropdown(0))));
                                    Bukkit.dispatchCommand(player, "res " + command + " " + residence.getName() + " " + input.trim());
                                    player.teleport(location);
                                }
                                sendResSensitiveOperationForm(player, residence);
                            }
                        })
        );
    }


    public static void sendResTrustedPlayerSettingForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        StringBuilder stringBuilder = new StringBuilder();
        List<String> trustedPlayers = Utils.getResTrustedPlayerString(residence);
        for (int i = 0, trustedPlayersSize = trustedPlayers.size(); i < trustedPlayersSize; i++) {
            String playerName = trustedPlayers.get(i);
            stringBuilder.append(playerName).append(i == trustedPlayersSize - 1 ? "" : ", ");
        }
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                SimpleForm.builder()
                        .title("领地信任玩家（成员）管理菜单")
                        .content("当前管理领地: " + residence.getName() + "\n信任玩家: " + stringBuilder + "\n§e信任玩家（成员）可以传送至该领地，并可以在领地内建造、挖掘、打开箱子等§r")
                        .button("添加/删除信任玩家")
                        .button("返回领地管理")
                        .responseHandler((f, r) -> {
                            SimpleFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                int id = response.getClickedButtonId();
                                if (id == 0) sendResTrustedPlayerAddForm(player, residence);
                                else if (id == 1) sendResSettingForm(player, residence);
                            }
                        })
        );
    }

    public static void sendResTrustedPlayerAddForm(Player player, ClaimedResidence residence) {
        if (residence == null) return;
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (!Utils.hasManagePermission(player, residence) && !player.isOp()) return;
        String[] playerNameList = Utils.getOnlinePlayerNameList();
        playerNameList[0] = "请选择玩家";
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地 §l" + residence.getName() + " §r§8信任玩家（成员）添加")
                        .dropdown("请选择在线玩家", playerNameList)
                        .input("仅在玩家不在线或上方表格人太多时才使用下方输入框", "完整玩家名称(包括大小写)")
                        .toggle("模式(关闭为添加,开启为删除)")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(1);
                                String flagState = response.getToggle(2) ? "remove" : "true";
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ")) {
                                    residence.getPermissions().setPlayerFlag(player, input, "trusted", flagState, false, false);
                                } else if (response.getDropdown(0) != 0) {
                                    residence.getPermissions().setPlayerFlag(player, playerNameList[response.getDropdown(0)], "trusted", flagState, false, false);
                                }
                                sendResTrustedPlayerSettingForm(player, residence);
                            }
                        })
        );

    }

    public static void sendResSettingForm(Player player) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        HashMap<String, ClaimedResidence> residenceList = Utils.getResidenceList(player);
        String[] resList = new String[residenceList.size() + 1];
        int i = 1;
        resList[0] = "请选择领地(此项=你所在的领地)";
        for (Map.Entry<String, ClaimedResidence> entry : residenceList.entrySet()) {
            resList[i++] = entry.getKey();
        }
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地管理选择")
                        .dropdown("可管理领地列表", resList)
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                if (response.getDropdown(0) == 0) {
                                    Residence res = Residence.getInstance();
                                    ClaimedResidence resClaim = res.getResidenceManager().getByLoc(player);
                                    if (resClaim == null || (!Utils.hasManagePermission(player, resClaim) && !player.isOp())) {
                                        sendMainResidenceForm(player);
                                        return;
                                    }
                                    sendResSettingForm(player, resClaim);
                                } else {
                                    sendResSettingForm(player, residenceList.get(resList[response.getDropdown(0)]));
                                }
                            }
                        })
        );
    }


    public static void sendResSetForm(Player player, ClaimedResidence residence) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (residence == null) return;
        if (!Utils.hasManagePermission(player, residence) && !player.isOp()) return;
        HashMap<String, FlagPermissions.FlagState> flags = Utils.getResidenceFlags(player, residence);
        CustomForm.Builder builder = CustomForm.builder();
        builder.title("§8领地 §l" + residence.getName() + " §r§8公共权限设置");
        int i = 0;
        HashMap<Integer, String> permissionList = new HashMap<>();
        for (Map.Entry<String, FlagPermissions.FlagState> entry : flags.entrySet()) {
            int flagPermission = Utils.flagToInt(entry.getValue());
            permissionList.put(i++, entry.getKey());
            Flags flag = Flags.getFlag(entry.getKey());
            String flagDec = flag != null ? "\n§e" + flag.getDesc() : "";
            builder.stepSlider("\n§a" + entry.getKey() + " §f权限" + flagDec + "\n§f权限状态§8", flagPermission, " §6禁用", " §6未设置（系统默认）", " §6启用");
        }
        builder.responseHandler((f, r) -> {
            CustomFormResponse response = f.parseResponse(r);
            if (response.isCorrect()) {
                HashMap<String, FlagPermissions.FlagState> newFlags = new HashMap<>();
                for (int j = 0; j < permissionList.size(); j++) {
                    newFlags.put(permissionList.get(j), Utils.intToFlag(response.getStepSlide(j)));
                }
                for (Map.Entry<String, FlagPermissions.FlagState> entry : flags.entrySet()) {
                    FlagPermissions.FlagState flagState = newFlags.get(entry.getKey());
                    if (flagState == null) ResidenceForm.getInstance().getLogger().severe("Flag state error: null");
                    if (flagState != entry.getValue()) residence.getPermissions().setFlag(entry.getKey(), flagState);
                }
                sendResSettingForm(player, residence);
            }
        });
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(builder);
    }

    public static void sendResPSetForm(Player player, ClaimedResidence residence) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (residence == null) return;
        if (!Utils.hasManagePermission(player, residence) && !player.isOp()) return;
        String[] playerNameList = Utils.getOnlinePlayerNameList();
        playerNameList[0] = "请选择玩家";
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(
                CustomForm.builder()
                        .title("§8领地 §l" + residence.getName() + " §r§8特定玩家权限设置")
                        .dropdown("选择要修改权限的玩家", playerNameList)
                        .input("如玩家不在线或上方表格人太多请使用输入框", "需要完整玩家名称(包括大小写)")
                        .responseHandler((f, r) -> {
                            CustomFormResponse response = f.parseResponse(r);
                            if (response.isCorrect()) {
                                String input = response.getInput(1);
                                if (input != null && !input.trim().equals("") && !input.trim().contains(" ")) {
                                    sendResPSetForm(player, input, residence);
                                } else if (response.getDropdown(0) != 0) {
                                    sendResPSetForm(player, playerNameList[response.getDropdown(0)], residence);
                                } else {
                                    sendResSettingForm(player, residence);
                                }
                            }
                        })
        );
    }

    public static void sendResPSetForm(Player player, String targetPlayer, ClaimedResidence residence) {
        UUID uuid = player.getUniqueId();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(uuid)) return;
        if (residence == null) return;
        if (!Utils.hasManagePermission(player, residence) && !player.isOp()) return;
        HashMap<String, FlagPermissions.FlagState> flags = Utils.getResidencePlayerFlags(player, targetPlayer, residence);
        CustomForm.Builder builder = CustomForm.builder();
        builder.title("§8领地 §l" + residence.getName() + " §r§8玩家 §l" + targetPlayer + " §r§8权限设置");
        int i = 0;
        HashMap<Integer, String> permissionList = new HashMap<>();
        for (Map.Entry<String, FlagPermissions.FlagState> entry : flags.entrySet()) {
            int flagPermission = Utils.flagToInt(entry.getValue());
            permissionList.put(i++, entry.getKey());
            Flags flag = Flags.getFlag(entry.getKey());
            String flagDec = flag != null ? "\n§e" + flag.getDesc() : "";
            builder.stepSlider("\n§a" + entry.getKey() + " §f权限" + flagDec + "\n§f权限状态§8", flagPermission, " §6禁用", " §6未设置(使用公共权限)", " §6启用");
        }
        builder.responseHandler((f, r) -> {
            CustomFormResponse response = f.parseResponse(r);
            if (response.isCorrect()) {
                HashMap<String, FlagPermissions.FlagState> newFlags = new HashMap<>();
                for (int j = 0; j < permissionList.size(); j++) {
                    newFlags.put(permissionList.get(j), Utils.intToFlag(response.getStepSlide(j)));
                }
                for (Map.Entry<String, FlagPermissions.FlagState> entry : flags.entrySet()) {
                    FlagPermissions.FlagState flagState = newFlags.get(entry.getKey());
                    if (flagState == null) ResidenceForm.getInstance().getLogger().severe("Flag state error: null");
                    if (flagState != entry.getValue())
                        residence.getPermissions().setPlayerFlag(targetPlayer, entry.getKey(), flagState);
                }
                sendResSettingForm(player, residence);
            }
        });
        FloodgateApi.getInstance().getPlayer(uuid).sendForm(builder);
    }
}
