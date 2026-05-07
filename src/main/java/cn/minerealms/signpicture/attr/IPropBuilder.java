package cn.minerealms.signpicture.attr;

/**
 * 属性构建器接口
 * 结合解析和差异计算
 */
public interface IPropBuilder<Diffed, Base> extends IPropParser, IPropDiff<Diffed, Base> {
}
