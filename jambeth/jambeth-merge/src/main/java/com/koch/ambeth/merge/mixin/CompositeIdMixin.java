package com.koch.ambeth.merge.mixin;

import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public class CompositeIdMixin {
    public boolean equalsCompositeId(ITypeInfoItem[] members, Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (left == right) {
            return true;
        }
        if (!left.getClass().equals(right.getClass())) {
            return false;
        }
        for (var member : members) {
            var leftValue = member.getValue(left, false);
            var rightValue = member.getValue(right, false);
            if (leftValue == null || rightValue == null) {
                return false;
            }
            if (!leftValue.equals(rightValue)) {
                return false;
            }
        }
        return true;
    }

    public int hashCodeCompositeId(ITypeInfoItem[] members, Object compositeId) {
        var hash = compositeId.getClass().hashCode();
        for (var member : members) {
            Object value = member.getValue(compositeId, false);
            if (value != null) {
                hash ^= value.hashCode();
            }
        }
        return hash;
    }

    public String toStringCompositeId(ITypeInfoItem[] members, Object compositeId) {
        var sb = new StringBuilder();
        toStringSbCompositeId(members, compositeId, sb);
        return sb.toString();
    }

    public void toStringSbCompositeId(ITypeInfoItem[] members, Object compositeId, StringBuilder sb) {
        // order does matter here
        for (int a = 0, size = members.length; a < size; a++) {
            var value = members[a].getValue(compositeId);
            if (a > 0) {
                sb.append('#');
            }
            if (value != null) {
                StringBuilderUtil.appendPrintable(sb, value);
            } else {
                sb.append("<null>");
            }
        }
    }
}
