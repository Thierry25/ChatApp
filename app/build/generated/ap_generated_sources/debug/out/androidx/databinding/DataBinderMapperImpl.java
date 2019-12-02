package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new marcelin.thierry.chatapp.DataBinderMapperImpl());
  }
}
