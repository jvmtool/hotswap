package com.github.jvmtool.hotswap;


import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ClassFileParser {

    /**
     * ClassFile {
     * u4             magic;
     * u2             minor_version;
     * u2             major_version;
     * u2             constant_pool_count;
     * cp_info        constant_pool[constant_pool_count-1];
     * u2             access_flags;
     * u2             this_class;
     * u2             super_class;
     * u2             interfaces_count;
     * u2             interfaces[interfaces_count];
     * u2             fields_count;
     * field_info     fields[fields_count];
     * u2             methods_count;
     * method_info    methods[methods_count];
     * u2             attributes_count;
     * attribute_info attributes[attributes_count];
     * }
     */
    static String getClassName(String classFile) throws Exception {
        FileInputStream fi = new FileInputStream(classFile);
        DataInputStream dis = new DataInputStream(fi);

        int offset = 0;
        //magic
        int magic = dis.readInt();
        //minor_version
        int minorVersion = dis.readUnsignedShort();
        //major_version
        int majorVersion = dis.readUnsignedShort();
        //constant_pool_count
        int constantPoolCount = dis.readUnsignedShort();
        //cp_info
        List<ConstantPoolItem> constantPool = new ArrayList<>(constantPoolCount - 1);
        for (int i = 0; i < constantPoolCount - 1; i++) {
            constantPool.add(ConstantPoolItem.parseConstantPoolItem(dis));
        }
        int access_flags = dis.readUnsignedShort();
        int this_class = dis.readUnsignedShort();
        int super_class = dis.readUnsignedShort();

        int interfaces_count = dis.readUnsignedShort();
        List<Integer> interfaces = new ArrayList<>(interfaces_count);
        for(int i=0;i<interfaces_count; i++) {
            interfaces.add(dis.readUnsignedShort());
        }

        int fields_count = dis.readUnsignedShort();
        List<FieldInfo> fields = new ArrayList<>(fields_count);
        for(int i=0;i<fields_count;i++) {
            fields.add(new FieldInfo(dis, constantPool));
        }

        int methods_count = dis.readUnsignedShort();
        List<MethodInfo> methods = new ArrayList<>(methods_count);
        for(int i=0;i<methods_count;i++) {
            methods.add(new MethodInfo(dis,constantPool));
        }

        int attributes_count = dis.readUnsignedShort();
        AttributeInfo[] attributes = new AttributeInfo[attributes_count];
        for(int j=0;j<attributes_count;j++) {
            attributes[j] = AttributeInfo.parseAttributeInfo(dis, constantPool);
        }
        return ((UTF8Item)constantPool.get(
                ((ClassItem)constantPool.get(this_class - 1)).index -1
        )).value.replaceAll("/", ".");
    }

    private static class ConstantPoolItem {
        private static final int CONSTANT_Class = 7;
        private static final int CONSTANT_Fieldref = 9;
        private static final int CONSTANT_Methodref = 10;
        private static final int CONSTANT_InterfaceMethodref = 11;
        private static final int CONSTANT_String = 8;
        private static final int CONSTANT_Integer = 3;
        private static final int CONSTANT_Float = 4;
        private static final int CONSTANT_Long = 5;
        private static final int CONSTANT_Double = 6;
        private static final int CONSTANT_NameAndType = 12;
        private static final int CONSTANT_Utf8 = 1;
        private static final int CONSTANT_MethodHandle = 15;
        private static final int CONSTANT_MethodType = 16;
        private static final int CONSTANT_InvokeDynamic = 18;
        int tag;

        private static ConstantPoolItem parseConstantPoolItem(DataInputStream dis)throws IOException{
            ConstantPoolItem item = null;
            int tag = dis.read();
            switch (tag) {
                case ConstantPoolItem.CONSTANT_Class:
                    ClassItem classItem = new ClassItem();
                    classItem.index = dis.readUnsignedShort();
                    item = classItem;
                    break;
                case ConstantPoolItem.CONSTANT_Fieldref:
                    FieldRefItem fieldRefItem = new FieldRefItem();
                    fieldRefItem.classIndex = dis.readUnsignedShort();
                    fieldRefItem.nameAndTypeIndex = dis.readUnsignedShort();
                    item = fieldRefItem;
                    break;
                case ConstantPoolItem.CONSTANT_Methodref:
                    MethodRefItem methodRefItem = new MethodRefItem();
                    methodRefItem.classIndex = dis.readUnsignedShort();
                    methodRefItem.nameAndTypeIndex = dis.readUnsignedShort();
                    item = methodRefItem;
                    break;
                case ConstantPoolItem.CONSTANT_InterfaceMethodref:
                    InterfaceMethodRefItem interfaceMethodRefItem = new InterfaceMethodRefItem();
                    interfaceMethodRefItem.classIndex = dis.readUnsignedShort();
                    interfaceMethodRefItem.nameAndTypeIndex = dis.readUnsignedShort();
                    item = interfaceMethodRefItem;
                    break;
                case ConstantPoolItem.CONSTANT_String:
                    StringItem stringItem = new StringItem();
                    stringItem.stringIndex = dis.readUnsignedShort();
                    item = stringItem;
                    break;
                case ConstantPoolItem.CONSTANT_Integer:
                    IntegerItem intergerItem = new IntegerItem();
                    intergerItem.value = dis.readInt();
                    item = intergerItem;
                    break;
                case ConstantPoolItem.CONSTANT_Float:
                    FloatItem floatItem = new FloatItem();
                    floatItem.value = dis.readFloat();
                    item = floatItem;
                    break;
                case ConstantPoolItem.CONSTANT_Long:
                    LongItem longItem = new LongItem();
                    longItem.value = dis.readLong();
                    item = longItem;
                    break;
                case ConstantPoolItem.CONSTANT_Double:
                    DoubleItem doubleItem = new DoubleItem();
                    doubleItem.value = dis.readDouble();
                    item = doubleItem;
                    break;
                case ConstantPoolItem.CONSTANT_NameAndType:
                    NameAndTypeItem nameAndTypeItem = new NameAndTypeItem();
                    nameAndTypeItem.nameIndex = dis.readUnsignedShort();
                    nameAndTypeItem.descriptorIndex = dis.readUnsignedShort();
                    item = nameAndTypeItem;
                    break;
                case ConstantPoolItem.CONSTANT_Utf8:
                    UTF8Item utf8Item = new UTF8Item();
                    utf8Item.value = dis.readUTF();
                    item = utf8Item;
                    break;
                case ConstantPoolItem.CONSTANT_MethodHandle:
                    MethodHandleItem methodHandleItem = new MethodHandleItem();
                    methodHandleItem.referenceKind = dis.read();
                    methodHandleItem.referenceIndex = dis.readUnsignedShort();
                    item = methodHandleItem;
                    break;
                case ConstantPoolItem.CONSTANT_MethodType:
                    MethodTypeItem methodTypeItem = new MethodTypeItem();
                    methodTypeItem.descriptorIndex = dis.readUnsignedShort();
                    item = methodTypeItem;
                    break;
                case ConstantPoolItem.CONSTANT_InvokeDynamic:
                    InvokeDynamicItem invokeDynamicItem = new InvokeDynamicItem();
                    invokeDynamicItem.bootstrapMethodAttrIndex = dis.readUnsignedShort();
                    invokeDynamicItem.nameAndTypeIndex = dis.readUnsignedShort();
                    item = invokeDynamicItem;
                    break;
                default:
                    throw new IllegalArgumentException("invalid tag");
            }
            item.tag = tag;
            return item;
        }
    }

    private static class ClassItem extends ConstantPoolItem{
        int index;
    }

    private static class FieldRefItem extends ConstantPoolItem{
        int classIndex;
        int nameAndTypeIndex;
    }

    private static class MethodRefItem extends ConstantPoolItem{
        int classIndex;
        int nameAndTypeIndex;
    }

    private static class InterfaceMethodRefItem extends ConstantPoolItem{
        int classIndex;
        int nameAndTypeIndex;
    }

    private static class StringItem extends ConstantPoolItem{
        int stringIndex;
    }

    private static class IntegerItem extends ConstantPoolItem{
        int value;
    }

    private static class FloatItem extends ConstantPoolItem{
        float value;
    }

    private static class DoubleItem extends ConstantPoolItem{
        double value;
    }

    private static class LongItem extends ConstantPoolItem{
        long value;
    }

    private static class NameAndTypeItem extends ConstantPoolItem{
        int nameIndex;
        int descriptorIndex;
    }

    private static class UTF8Item extends ConstantPoolItem{
        String value;
    }

    private static class MethodHandleItem extends ConstantPoolItem{
        int referenceKind;
        int referenceIndex;
    }

    private static class MethodTypeItem extends ConstantPoolItem{
        int descriptorIndex;
    }

    private static class InvokeDynamicItem extends ConstantPoolItem{
        int bootstrapMethodAttrIndex;
        int nameAndTypeIndex;
    }

    private static class FieldInfo{
        int             accessFlags;
        int             nameIndex;
        int             descriptorIndex;
        int             attributesCount;
        AttributeInfo[] attributes;

        String name;

        private FieldInfo(DataInputStream dis, List<ConstantPoolItem> constantPool) throws IOException{
            this.accessFlags = dis.readUnsignedShort();
            this.nameIndex = dis.readUnsignedShort();
            this.descriptorIndex = dis.readUnsignedShort();
            this.attributesCount = dis.readUnsignedShort();
            this.attributes = new AttributeInfo[this.attributesCount];
            for(int j=0;j<this.attributesCount;j++) {
                this.attributes[j] = AttributeInfo.parseAttributeInfo(dis, constantPool);
            }
            this.name = ((UTF8Item)constantPool.get(nameIndex-1)).value;
        }
    }

    private static class MethodInfo {
        int accessFlags;
        int nameIndex;
        int descriptorIndex;
        int attributesCount;
        AttributeInfo[] attributes;

        String name;

        private MethodInfo(DataInputStream dis, List<ConstantPoolItem> constantPool) throws IOException{;
            this.accessFlags = dis.readUnsignedShort();
            this.nameIndex = dis.readUnsignedShort();
            this.descriptorIndex = dis.readUnsignedShort();
            this.attributesCount = dis.readUnsignedShort();
            this.attributes = new AttributeInfo[this.attributesCount];
            for(int j=0;j<this.attributesCount;j++) {
                this.attributes[j] = AttributeInfo.parseAttributeInfo(dis, constantPool);
            }
            this.name = ((UTF8Item)constantPool.get(nameIndex-1)).value;
        }
    }

    private static class AttributeInfo{
        int attributeNameIndex;
        int attributeLength;
        byte[] info;

        private AttributeInfo(){}

        private AttributeInfo(int attributeNameIndex, int attributeLength) {
            this.attributeNameIndex = attributeNameIndex;
            this.attributeLength = attributeLength;
        }

        String attributeName;

        private static AttributeInfo parseAttributeInfo(DataInputStream dis,
                                                        List<ConstantPoolItem> constantPool) throws IOException {
            AttributeInfo attributeInfo = new AttributeInfo(
                    dis.readUnsignedShort(),
                    dis.readInt()
            );
            byte[] info = new byte[attributeInfo.attributeLength];
            dis.read(info);
            attributeInfo.info = info;
            attributeInfo.attributeName = ((UTF8Item)constantPool.get(attributeInfo.attributeNameIndex -1)).value;
            return attributeInfo;
        }
    }

    private static class ConstantValueAttr extends AttributeInfo{
        int constantvalue_index;
    }

    /**
     Code_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 max_stack;
     u2 max_locals;
     u4 code_length;
     u1 code[code_length];
     u2 exception_table_length;
     {   u2 start_pc;
     u2 end_pc;
     u2 handler_pc;
     u2 catch_type;
     } exception_table[exception_table_length];
     u2 attributes_count;
     attribute_info attributes[attributes_count];
     }
     */
    private static class CodeAttr extends AttributeInfo{
        int max_stack;
        int max_locals;
        int code_length;
        byte code[];
        int exception_table_length;
        ExceptionTable[] exceptionTable;
        int attributes_count;
        AttributeInfo[] attributes;
    }

    private static class ExceptionTable{
        int start_pc;
        int end_pc;
        int handler_pc;
        int catch_type;
    }

    /**
     StackMapTable_attribute {
     u2              attribute_name_index;
     u4              attribute_length;
     u2              number_of_entries;
     stack_map_frame entries[number_of_entries];
     }
     */
    private static class StackMapTableAttr extends AttributeInfo{
        int              number_of_entries;
        StackMapFrame entries[];
    }

    /**
     * Exceptions_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 number_of_exceptions;
     u2 exception_index_table[number_of_exceptions];
     }
     */
    private static class ExceptionsAttr extends AttributeInfo {
        int number_of_exceptions;
        int exception_index_table[];
    }

    /**
     * InnerClasses_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 number_of_classes;
     {   u2 inner_class_info_index;
     u2 outer_class_info_index;
     u2 inner_name_index;
     u2 inner_class_access_flags;
     } classes[number_of_classes];
     }
     */
    private static class InnerClassesAttr extends AttributeInfo {
        int number_of_classes;
        InnerClass classes[];
    }

    private static class InnerClass {
        int inner_class_info_index;
        int outer_class_info_index;
        int inner_name_index;
        int inner_class_access_flags;
    }

    /**
     * EnclosingMethod_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 class_index;
     u2 method_index;
     }
     */
    private static class EnclosingMethodAttr extends AttributeInfo {
        int class_index;
        int method_index;
    }

    /**
     * Synthetic_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     }
     */
    private static class SyntheticAttr extends AttributeInfo {

    }

    /**
     * Signature_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 signature_index;
     }
     */
    private static class SignatureAttr extends AttributeInfo{
        int signature_index;
    }

    /**
     * SourceFile_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 sourcefile_index;
     }
     */
    private static class SourceFileAttr extends AttributeInfo {
        int sourcefile_index;
    }

    /**
     * SourceDebugExtension_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u1 debug_extension[attribute_length];
     }
     */
    private static class SourceDebugExtensionAttr extends AttributeInfo {
        byte[] debug_extension;
    }

    /**
     * LineNumberTable_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 line_number_table_length;
     {   u2 start_pc;
     u2 line_number;
     } line_number_table[line_number_table_length];
     }
     */
    private static class LineNumberTableAttr extends AttributeInfo{
        int line_number_table[][];
    }

    /**
     * LocalVariableTable_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 local_variable_table_length;
     {   u2 start_pc;
     u2 length;
     u2 name_index;
     u2 descriptor_index;
     u2 index;
     } local_variable_table[local_variable_table_length];
     }
     */
    private static class LocalVariableTableAttr extends AttributeInfo {
        int local_variable_table_length;
        LocalVariableTable local_variable_table[];
    }

    private static class LocalVariableTable{
        int start_pc;
        int length;
        int name_index;
        int descriptor_index;
        int index;
    }

    /**
     * LocalVariableTypeTable_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u2 local_variable_type_table_length;
     {   u2 start_pc;
     u2 length;
     u2 name_index;
     u2 signature_index;
     u2 index;
     } local_variable_type_table[local_variable_type_table_length];
     }
     */
    private static class LocalVariableTypeTableAttr extends AttributeInfo {
        int local_variable_type_table_length;
        LocalVariableTypeTable local_variable_type_table[];
    }

    private static class LocalVariableTypeTable {
        int start_pc;
        int length;
        int name_index;
        int signature_index;
        int index;
    }

    private static class DeprecatedAttr extends AttributeInfo {

    }

    /**
     * RuntimeVisibleAnnotations_attribute {
     u2         attribute_name_index;
     u4         attribute_length;
     u2         num_annotations;
     annotation annotations[num_annotations];
     }
     */
    private static class RuntimeVisibleAnnotationsAttr extends AttributeInfo {
        int         num_annotations;
        Annotation annotations[];
    }

    /**
     * annotation {
     u2 type_index;
     u2 num_element_value_pairs;
     {   u2            element_name_index;
     element_value value;
     } element_value_pairs[num_element_value_pairs];
     }
     */
    private static class Annotation {
        int type_index;
        int num_element_value_pairs;
        ElementValuePair element_value_pairs[];
    }

    private static class ElementValuePair {
        int element_name_index;
        ElementValuePair value;
    }

    /**
     * element_value {
     u1 tag;
     union {
     u2 const_value_index;

     {   u2 type_name_index;
     u2 const_name_index;
     } enum_const_value;

     u2 class_info_index;

     annotation annotation_value;

     {   u2            num_values;
     element_value values[num_values];
     } array_value;
     } value;
     }
     */
    private static class ElementValue{
        int tag;
        int const_value_index;

        int type_name_index;
        int const_name_index;

        int class_info_index;

        Annotation annotation_value;

        int num_values;
        ElementValue values[];
    }

    private static class RuntimeInvisibleAnnotations extends AttributeInfo{
        int num_annotations;
        Annotation annotations[];
    }

    /**
     * RuntimeVisibleParameterAnnotations_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u1 num_parameters;
     {   u2         num_annotations;
     annotation annotations[num_annotations];
     } parameter_annotations[num_parameters];
     }
     */
    private static class RuntimeVisibleParameterAnnotations extends AttributeInfo{
        int num_parameters;
        Pair<Integer, Annotation[]>[] parameter_annotations;
    }

    private static class RuntimeInvisibleParameterAnnotations extends AttributeInfo {
        int num_parameters;
        Pair<Integer, Annotation[]>[] parameter_annotations;
    }

    /**
     * RuntimeVisibleTypeAnnotations_attribute {
     u2              attribute_name_index;
     u4              attribute_length;
     u2              num_annotations;
     type_annotation annotations[num_annotations];
     }
     */
    private static class RuntimeVisibleTypeAnnotations extends AttributeInfo{

        int              num_annotations;
        //todo:
//        type_annotation annotations[];
    }

    private static class RuntimeInvisibleTypeAnnotations extends AttributeInfo{
        int              num_annotations;
        //        type_annotation annotations[];
    }

    private static class AnnotationDefault extends AttributeInfo{

    }

    private static class BootstrapMethods extends AttributeInfo {

    }

    /**
     * MethodParameters_attribute {
     u2 attribute_name_index;
     u4 attribute_length;
     u1 parameters_count;
     {   u2 name_index;
     u2 access_flags;
     } parameters[parameters_count];
     }
     */
    private static class MethodParameters extends AttributeInfo{
        int parameters_count;
        Pair<Integer, Integer>[] parameters;
    }


    private static class Pair<T,D>{
        T first;
        D second;
    }

    /**
     union stack_map_frame {
     same_frame;
     same_locals_1_stack_item_frame;
     same_locals_1_stack_item_frame_extended;
     chop_frame;
     same_frame_extended;
     append_frame;
     full_frame;
     }
     */
    private static class StackMapFrame {
        int frame_type;
    }

    /**
     same_frame {
     u1 frame_type = SAME; // 0-63
     }
     */
    private static class SameFrame extends StackMapFrame{}

    /**
    same_locals_1_stack_item_frame {
        u1 frame_type = SAME_LOCALS_1_STACK_ITEM; // 64-127
        verification_type_info stack[1];
    }
    */
    private static class SameLocals1StackItemFrame extends StackMapFrame{
        VerificationTypeInfo stack[];
    }

    /**
     same_locals_1_stack_item_frame_extended {
     u1 frame_type = SAME_LOCALS_1_STACK_ITEM_EXTENDED;  247
    u2 offset_delta;
    verification_type_info stack[1];
     }
     */
    private static class SameLocals1StackItemFrameExtended extends StackMapFrame{
        int offset_delta;
        VerificationTypeInfo stack[];
    }

    /**
     * chop_frame {
     u1 frame_type = CHOP; // 248-250
    u2 offset_delta;
     }
     */
    private static class ChopFrame extends StackMapFrame{
        int offset_delta;
    }

    /**
     *append_frame {
     u1 frame_type = APPEND; // 252-254
    u2 offset_delta;
    verification_type_info locals[frame_type - 251];
     }
     */
    private static class AppendFrame extends StackMapFrame{
        int offset_delta;
        VerificationTypeInfo stack[];
    }

    /**
     * full_frame {
     u1 frame_type = FULL_FRAME; // 255
    u2 offset_delta;
    u2 number_of_locals;
    verification_type_info locals[number_of_locals];
    u2 number_of_stack_items;
    verification_type_info stack[number_of_stack_items];
     }
     */
    private static class FullFrame extends StackMapFrame{
        int offset_delta;
        int number_of_locals;
        VerificationTypeInfo locals[];
        int number_of_stack_items;
        VerificationTypeInfo stack[];
    }

    /**
     union verification_type_info {
     Top_variable_info;
     Integer_variable_info;
     Float_variable_info;
     Long_variable_info;
     Double_variable_info;
     Null_variable_info;
     UninitializedThis_variable_info;
     Object_variable_info;
     Uninitialized_variable_info;
     }
     */
    private static class VerificationTypeInfo {
        int tag;
    }

    /**
     Top_variable_info {
     u1 tag = ITEM_Top; /* 0
     }
     */
    private static class TopVariableInfo extends VerificationTypeInfo {
    }

    /**
     Integer_variable_info {
     u1 tag = ITEM_Integer; /* 1
     }
     */
    private static class Integer_variable_info extends VerificationTypeInfo {
    }

    /**
     * Float_variable_info {
     u1 tag = ITEM_Float; /* 2
     }
     */
    private static class Float_variable_info extends VerificationTypeInfo {
    }

    /**
     Null_variable_info {
     u1 tag = ITEM_Null; /* 5
     }
     */
    private static class Null_variable_info extends VerificationTypeInfo {
    }

    /**
     UninitializedThis_variable_info {
     u1 tag = ITEM_UninitializedThis; /* 6
     }
     */
    private static class UninitializedThis_variable_info extends VerificationTypeInfo {
    }

    /**
     Object_variable_info {
     u1 tag = ITEM_Object; /* 7
    u2 cpool_index;
     }
     */
    private static class Object_variable_info extends VerificationTypeInfo {
        int cpool_index;
    }

    /**
     Uninitialized_variable_info {
     u1 tag = ITEM_Uninitialized; /* 8
     u2 offset;
     */
    private static class Uninitialized_variable_info extends VerificationTypeInfo {
        int offset;
    }

    /**
     Long_variable_info {
     u1 tag = ITEM_Long; /* 4
     }
     */
    private static class Long_variable_info extends VerificationTypeInfo {
    }

    /**
     Long_variable_info {
     u1 tag = ITEM_Long; /* 4
     }
     */
    private static class Double_variable_info extends VerificationTypeInfo {
    }
}
