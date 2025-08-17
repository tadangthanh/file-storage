package vn.thanh.metadataservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public final class Perms {
    public static final int READ = 1 << 0; // 1
    public static final int WRITE = 1 << 1; // 2
    public static final int DELETE = 1 << 2; // 4
    public static final int SHARE = 1 << 3; // 8


    public static List<String> toList(int mask) {
        List<String> result = new ArrayList<>();
        if ((mask & READ) != 0) result.add("READ");
        if ((mask & WRITE) != 0) result.add("WRITE");
        if ((mask & DELETE) != 0) result.add("DELETE");
        if ((mask & SHARE) != 0) result.add("SHARE");
        return result;
    }

    public static boolean has(int mask, int p) {
        return (mask & p) != 0;
    }

    public static int add(int mask, int p) {
        return mask | p;
    }

    public static int remove(int mask, int p) {
        return mask & ~p;
    }


}
