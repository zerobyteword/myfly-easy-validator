package io.github.zerobyteword.easyvalidator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.zerobyteword.easyvalidator.beans.TestComplexBean1;
import io.github.zerobyteword.easyvalidator.beans.TestComplexBean2;
import io.github.zerobyteword.easyvalidator.beans.TestComplexBean3;
import io.github.zerobyteword.easyvalidator.beans.TestComplexBean4;
import io.github.zerobyteword.easyvalidator.beans.TestComplexBean5;
import io.github.zerobyteword.easyvalidator.beans.TestMapListBean1;
import io.github.zerobyteword.easyvalidator.beans.TestMapListBean2;
import io.github.zerobyteword.easyvalidator.beans.TestMapTypeBean1;
import io.github.zerobyteword.easyvalidator.beans.TestMapTypeBean2;
import io.github.zerobyteword.easyvalidator.beans.TestMappingBean1;
import io.github.zerobyteword.easyvalidator.beans.TestMappingBean2;
import io.github.zerobyteword.easyvalidator.beans.TestUseStrictBean1;
import io.github.zerobyteword.easyvalidator.beans.TestUseStrictBean2;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class DoubleCheckSourceBean {

    @Test
    public void testMapping() {
        TestMappingBean1 sourceBean = new TestMappingBean1();
        sourceBean.setName("foo");
        TestMappingBean2 targetBean = new TestMappingBean2();
        targetBean.setName2("foo");
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .mapping("$.name", "$.name2")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }

    @Test
    public void testMapping_nomatch() {
        TestMappingBean1 sourceBean = new TestMappingBean1();
        sourceBean.setName("foo");
        TestMappingBean2 targetBean = new TestMappingBean2();
        targetBean.setName2("foo1");
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .mapping("$.name", "$.name2")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
    }

    @Test
    public void testStrict_noUseStrict() {
        TestUseStrictBean1 sourceBean = new TestUseStrictBean1();
        sourceBean.setAge("18");
        sourceBean.setTest("true");
        sourceBean.setTest2(1);
        TestUseStrictBean2 targetBean = new TestUseStrictBean2();
        targetBean.setAge(18);
        targetBean.setTest(true);
        targetBean.setTest2(true);
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }

    @Test
    public void testStrict_UseStrict() {
        TestUseStrictBean1 sourceBean = new TestUseStrictBean1();
        sourceBean.setAge("18");
        sourceBean.setTest("true");
        sourceBean.setTest2(1);
        TestUseStrictBean2 targetBean = new TestUseStrictBean2();
        targetBean.setAge(18);
        targetBean.setTest(true);
        targetBean.setTest2(true);
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
        Assert.assertEquals(3, checkResult.size());
    }

    @Test
    public void testStrict_MapType_simple() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        sourceBean.setMap(ImmutableMap.of("name", "xiaoming"));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        targetBean.setMap(ImmutableMap.of("name", "xiaoming"));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }

    @Test
    public void testStrict_MapType_simple_nomatch() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        sourceBean.setMap(ImmutableMap.of("name", "xiaoming"));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        targetBean.setMap(ImmutableMap.of("name", "xiaoming1"));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
    }

    @Test
    public void testStrict_MapType_simple_missKey() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        sourceBean.setMap(ImmutableMap.of("name1", "xiaoming"));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        targetBean.setMap(ImmutableMap.of("name2", "xiaoming1"));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
    }

    @Test
    public void testStrict_MapType_simple_mapping() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        sourceBean.setMap(ImmutableMap.of("name1", "xiaoming"));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        targetBean.setMap(ImmutableMap.of("name2", "xiaoming"));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .mapping("$.map[name1]", "$.map[name2]")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }

    @Test
    public void testStrict_MapType() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        TestMappingBean1 bean1 = new TestMappingBean1();
        bean1.setName("test");
        sourceBean.setMap(ImmutableMap.of("name", bean1));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        TestMappingBean1 bean2 = new TestMappingBean1();
        bean2.setName("test");
        targetBean.setMap(ImmutableMap.of("name", bean2));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }


    @Test
    public void testStrict_MapType_deepMapping() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        TestMappingBean1 bean1 = new TestMappingBean1();
        bean1.setName("test");
        sourceBean.setMap(ImmutableMap.of("name", bean1));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        TestMappingBean1 bean2 = new TestMappingBean1();
        bean2.setName("test");
        targetBean.setMap(ImmutableMap.of("name2", bean2));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .mapping("$.map[name]", "$.map[name2]")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }


    @Test
    public void testStrict_MapType_deepMapping_noMatch() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        TestMappingBean1 bean1 = new TestMappingBean1();
        bean1.setName("test");
        sourceBean.setMap(ImmutableMap.of("name", bean1));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        TestMappingBean1 bean2 = new TestMappingBean1();
        bean2.setName("test1");
        targetBean.setMap(ImmutableMap.of("name2", bean2));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .mapping("$.map[name]", "$.map[name2]")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
    }


    @Test
    public void testStrict_MapType_noMatch() {
        TestMapTypeBean1 sourceBean = new TestMapTypeBean1();
        TestMappingBean1 bean1 = new TestMappingBean1();
        bean1.setName("test");
        sourceBean.setMap(ImmutableMap.of("name", bean1));
        TestMapTypeBean2 targetBean = new TestMapTypeBean2();
        TestMappingBean1 bean2 = new TestMappingBean1();
        bean2.setName("test2");
        targetBean.setMap(ImmutableMap.of("name", bean2));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
    }

    @Test
    public void testStrict_ListType_strict_Match() {
        TestMapListBean1 sourceBean = new TestMapListBean1();
        sourceBean.setList(Lists.newArrayList("123", "456"));
        TestMapListBean2 targetBean = new TestMapListBean2();
        targetBean.setList(Lists.newArrayList("123", "456"));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }

    @Test
    public void testStrict_ListType_strict_noMatch() {
        TestMapListBean1 sourceBean = new TestMapListBean1();
        sourceBean.setList(Lists.newArrayList("123", "456"));
        TestMapListBean2 targetBean = new TestMapListBean2();
        targetBean.setList(Lists.newArrayList("123", "459"));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
    }


    @Test
    public void testStrict_ListBeanType_strict_Match() {
        TestMapListBean1 sourceBean = new TestMapListBean1();
        TestMappingBean1 bean1 = new TestMappingBean1();
        bean1.setName("test");
        sourceBean.setList(Lists.newArrayList(bean1));
        TestMapListBean2 targetBean = new TestMapListBean2();
        TestMappingBean1 bean2 = new TestMappingBean1();
        bean2.setName("test");
        targetBean.setList(Lists.newArrayList(bean2));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertTrue(checkResult.isEmpty());
    }


    @Test
    public void testStrict_ListBeanType_strict_noMatch() {
        TestMapListBean1 sourceBean = new TestMapListBean1();
        TestMappingBean1 bean1 = new TestMappingBean1();
        bean1.setName("test");
        sourceBean.setList(Lists.newArrayList(bean1));
        TestMapListBean2 targetBean = new TestMapListBean2();
        TestMappingBean1 bean2 = new TestMappingBean1();
        bean2.setName("test2");
        targetBean.setList(Lists.newArrayList(bean2));
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .build();
        List<DiffItem> checkResult = doubleCheck.check(sourceBean, targetBean);
        Assert.assertFalse(checkResult.isEmpty());
    }

    @Test
    public void testStrict_ComplexType_matched() {
        TestComplexBean5 testComplexBean5 = new TestComplexBean5();
        testComplexBean5.setTest5("test5");
        testComplexBean5.setAge5("5");
        TestComplexBean4 testComplexBean4 = new TestComplexBean4();
        testComplexBean4.setAge2("2");
        testComplexBean4.setTest("test2");
        testComplexBean4.setTestComplexBean5(testComplexBean5);
        TestComplexBean3 testComplexBean3 = new TestComplexBean3();
        testComplexBean3.setTestComplexBean5(testComplexBean5);
        testComplexBean3.setTest("test2");
        testComplexBean3.setAge("2");
        TestComplexBean2 testComplexBean2 = new TestComplexBean2();
        testComplexBean2.setName2("name");
        testComplexBean2.setTestComplexBean4(testComplexBean4);
        TestComplexBean1 testComplexBean1 = new TestComplexBean1();
        testComplexBean1.setName("name");
        testComplexBean1.setTestComplexBean3(testComplexBean3);
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .mapping("$.name", "$.name2")
                .mapping("$.testComplexBean3.age", "$.testComplexBean4.age2")
                .mapping("$.testComplexBean3", "$.testComplexBean4")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(testComplexBean1, testComplexBean2);
        Assert.assertTrue(checkResult.isEmpty());
    }

    @Test
    public void testStrict_ComplexType_noMatched() {
        TestComplexBean5 testComplexBean5 = new TestComplexBean5();
        testComplexBean5.setTest5("test5");
        testComplexBean5.setAge5("5");
        TestComplexBean4 testComplexBean4 = new TestComplexBean4();
        testComplexBean4.setAge2("2");
        testComplexBean4.setTest("test3");
        testComplexBean4.setTestComplexBean5(testComplexBean5);
        TestComplexBean3 testComplexBean3 = new TestComplexBean3();
        testComplexBean3.setTestComplexBean5(testComplexBean5);
        testComplexBean3.setTest("test2");
        testComplexBean3.setAge("2");
        TestComplexBean2 testComplexBean2 = new TestComplexBean2();
        testComplexBean2.setName2("name");
        testComplexBean2.setTestComplexBean4(testComplexBean4);
        TestComplexBean1 testComplexBean1 = new TestComplexBean1();
        testComplexBean1.setName("name");
        testComplexBean1.setTestComplexBean3(testComplexBean3);
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .mapping("$.name", "$.name2")
                .mapping("$.testComplexBean3.age", "$.testComplexBean4.age2")
                .mapping("$.testComplexBean3", "$.testComplexBean4")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(testComplexBean1, testComplexBean2);
        Assert.assertFalse(checkResult.isEmpty());
    }

    @Test
    public void testStrict_ComplexType_excludeSame() {
        TestComplexBean5 testComplexBean5 = new TestComplexBean5();
        testComplexBean5.setTest5("test5");
        testComplexBean5.setAge5("5");
        TestComplexBean4 testComplexBean4 = new TestComplexBean4();
        testComplexBean4.setAge2("2");
        testComplexBean4.setTest("test3");
        testComplexBean4.setTestComplexBean5(testComplexBean5);
        TestComplexBean3 testComplexBean3 = new TestComplexBean3();
        testComplexBean3.setTestComplexBean5(testComplexBean5);
        testComplexBean3.setTest("test2");
        testComplexBean3.setAge("2");
        TestComplexBean2 testComplexBean2 = new TestComplexBean2();
        testComplexBean2.setName2("name");
        testComplexBean2.setTestComplexBean4(testComplexBean4);
        TestComplexBean1 testComplexBean1 = new TestComplexBean1();
        testComplexBean1.setName("name");
        testComplexBean1.setTestComplexBean3(testComplexBean3);
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .mapping("$.name", "$.name2")
                .exclude("$.testComplexBean3")
                .build();
        List<DiffItem> checkResult = doubleCheck.check(testComplexBean1, testComplexBean2);
        Assert.assertTrue(checkResult.isEmpty());
    }


    @Test
    public void testStrict_ComplexType_subMapping() {
        TestComplexBean5 testComplexBean5 = new TestComplexBean5();
        testComplexBean5.setTest5("test5");
        testComplexBean5.setAge5("5");
        TestComplexBean4 testComplexBean4 = new TestComplexBean4();
        testComplexBean4.setAge2("2");
        testComplexBean4.setTest("test2");
        testComplexBean4.setTestComplexBean5(testComplexBean5);
        TestComplexBean3 testComplexBean3 = new TestComplexBean3();
        testComplexBean3.setTestComplexBean5(testComplexBean5);
        testComplexBean3.setTest("test2");
        testComplexBean3.setAge("2");
        TestComplexBean2 testComplexBean2 = new TestComplexBean2();
        testComplexBean2.setName2("name");
        testComplexBean2.setTestComplexBean4(testComplexBean4);
        TestComplexBean1 testComplexBean1 = new TestComplexBean1();
        testComplexBean1.setName("name");
        testComplexBean1.setTestComplexBean3(testComplexBean3);
        DoubleCheck doubleCheck = DoubleCheck.newBuilder()
                .useStrict(true)
                .mapping("$.name", "$.name2")
                .mapping("$.testComplexBean3", "$.testComplexBean4", handler -> {
                     handler.mapping("age", "age2");
                })
                .build();
        List<DiffItem> checkResult = doubleCheck.check(testComplexBean1, testComplexBean2);
        Assert.assertTrue(checkResult.isEmpty());
    }

}