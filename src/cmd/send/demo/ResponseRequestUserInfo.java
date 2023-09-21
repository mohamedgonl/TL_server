package cmd.send.demo;

import bitzero.server.extensions.data.BaseMsg;

import cmd.CmdDefine;

import java.nio.ByteBuffer;

import model.PlayerInfo;

public class ResponseRequestUserInfo extends BaseMsg {
    public PlayerInfo info;

    public ResponseRequestUserInfo(short error) {
        super(CmdDefine.GET_USER_INFO, error);
    }

    public ResponseRequestUserInfo(short error, PlayerInfo _info) {
        super(CmdDefine.GET_USER_INFO, error);
        info = _info;
    }

    @Override
    public byte[] createData() {
        ByteBuffer bf = makeBuffer();
        putStr(bf, info.getName());
        putStr(bf, info.getAvatar());
        bf.putInt(info.getLevel());
        bf.putInt(info.getRank());
        bf.putInt(info.getGold());
        bf.putInt(info.getElixir());
        bf.putInt(info.getGem());
        return packBuffer(bf);
    }
}
