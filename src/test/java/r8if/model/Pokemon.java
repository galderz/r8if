package r8if.model;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoMessage;

@ProtoDoc("@Indexed")
@ProtoMessage(name = "Pokemon")
public class Pokemon {

   public final String name;
   public final String type;

   public Pokemon(String name, String type) {
      this.name = name;
      this.type = type;
   }

   // @ProtoDoc("@Field(index = Index.NO, store = Store.NO)")
   // TODO Is ^ needed to avoid be indexed?
   @ProtoField(number = 1, required = true)
   public String getName() {
      return name;
   }

   @ProtoDoc("@IndexedField")
   @ProtoField(number = 2, required = true)
   public String getType() {
      return type;
   }

   @Override
   public String toString() {
      return "Pokemon{" +
         "name='" + name + '\'' +
         ", type='" + type + '\'' +
         '}';
   }

//   public static final class Marshaller implements MessageMarshaller<Pokemon> {
//
//      @Override
//      public Pokemon readFrom(ProtoStreamReader reader) throws IOException {
//         final String name = reader.readString("name");
//         final String type = reader.readString("type");
//         return new Pokemon(name, type);
//      }
//
//      @Override
//      public void writeTo(ProtoStreamWriter writer, Pokemon obj) throws IOException {
//         writer.writeString("name", obj.name);
//         writer.writeString("type", obj.type);
//      }
//
//      @Override
//      public Class<? extends Pokemon> getJavaClass() {
//         return Pokemon.class;
//      }
//
//      @Override
//      public String getTypeName() {
//         return "r8if.model.Pokemon";
//      }
//
//   }

}
