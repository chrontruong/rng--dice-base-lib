package com.io.begstd.slot.model.gamerule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.io.begstd.slot.model.playsession.ISymbol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
@Builder (toBuilder =  true)
@NoArgsConstructor
@AllArgsConstructor
public class Symbol implements ISymbol {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int id;

    private String code;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SymbolType type;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Integer> paytable;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Float> paytableFloat;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Integer> paytableRight;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Integer> freespin;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Integer> bonusgame;


    /*
     * ----- Constructors
     */
    /*
     * private Symbol(@NonNull Integer id, @NonNull String name, @NonNull
     * List<Integer> paytable) { this.id = id; this.name = name; this.type = SYMBOL;
     * this.paytable = paytable; } private Symbol(@NonNull Integer id, @NonNull
     * String name, @NonNull SymbolType type) { this.id = id; this.name = name;
     * this.type = type; } private Symbol(@NonNull Integer id,
     * @NonNull String name,
     * @NonNull SymbolType type,
     * @NonNull List<Integer> paytable,
     * @NonNull List<Integer> freespin ) { this.id = id; this.name = name; this.type
     * = type; this.paytable = paytable; this.freespin = freespin; }
     */

    public boolean isAnim() {
        return type == SymbolType.WILD_ANIM;
    }

    /*
     * ----- Static Constructors
     */
    /*
     * public static Symbol ofNormal(@NonNull Integer id, @NonNull String
     * name, @NonNull List<Integer> paytable) { return new Symbol(id, name,
     * paytable); } public static Symbol ofWild(@NonNull Integer id, @NonNull String
     * name) { return new Symbol(id, name, WILD); } public static Symbol
     * ofAnim(@NonNull Integer id, @NonNull String name) { return new Symbol(id,
     * name, WILD_ANIM); } public static Symbol ofScatterFreeSpin(@NonNull Integer
     * id, @NonNull String name, @NonNull List<Integer> paytable, @NonNull
     * List<Integer> freespin) { return new Symbol(id, name, SCATTER, paytable,
     * freespin); } public static Symbol ofScatterMiniGame(@NonNull Integer
     * id, @NonNull String name) { return new Symbol(id, name, MINI_GAME); }
     */

    /*
     * ----- Method
     */
    public boolean equalTo(Symbol that) {

        if (this == that)
            return true;
        if (that == null || getClass() != that.getClass())
            return false;

        if (this.type == SymbolType.WILD || this.type == SymbolType.WILD_ANIM || that.type == SymbolType.WILD
                || that.type == SymbolType.WILD_ANIM) {
            return true;
        }

        return (id == that.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Symbol symbol = (Symbol) o;
        return (id == symbol.id) && name.equals(symbol.name) && type == symbol.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type);
    }

    @Override
    public String toString() {
        return "Symbol{" + "code=" + code + ", name='" + name + '\'' + ", type=" + type + '}';
    }
}
