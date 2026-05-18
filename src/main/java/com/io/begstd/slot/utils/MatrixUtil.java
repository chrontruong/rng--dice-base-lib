package com.io.begstd.slot.utils;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.model.playsession.ISymbol;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.*;

@Slf4j
public class MatrixUtil {
    
    /**
     * 
     * @param tableFormat : {3, 4, 5, 4, 3}
     * @return vertical matrix
     * [x, x, x]
     * [x, x, x, x]
     * [x, x, x, x, x]
     * [x, x, x, x]
     * [x, x, x]
     * 
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] initVerticalMatrix(Class<T> clazz, List<Integer> tableFormat) {
        
        T[][] matrix = (T[][]) Array.newInstance(clazz, tableFormat.size(), 0);
        
        for (int reelIdx = 0, reelSize = tableFormat.size(); reelIdx < reelSize; reelIdx++) {
            matrix[reelIdx] = (T[]) Array.newInstance(clazz, tableFormat.get(reelIdx));
        }
        
        return matrix;
    }
    
    public static <T> T[][] initVerticalMatrix(Class<T> clazz, int... dimensions) {
        
        @SuppressWarnings("unchecked")
        T[][] matrix = (T[][]) Array.newInstance(clazz, dimensions);
        
        return matrix;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T[][] reverseMatrix(T[][] matrix) {
        Class<T> elementType = (Class<T>) matrix.getClass().getComponentType().getComponentType();
        int reverseMatrixRowSzie = 0; // is max length of each current matrix row.

        for (T[] oldRow : matrix) {
            if (oldRow.length > reverseMatrixRowSzie) {
                reverseMatrixRowSzie = oldRow.length;
            }
        }
        
        T[][] reverseMatrix = (T[][]) Array.newInstance(elementType, reverseMatrixRowSzie, matrix.length);

        for (int rowIdx = 0; rowIdx < matrix.length; rowIdx++) {
            for (int reelIdx = 0; reelIdx < matrix[rowIdx].length; reelIdx++) {
                reverseMatrix[reelIdx][rowIdx] = matrix[rowIdx][reelIdx];
            }
        }

        return reverseMatrix;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T[][] cloneMatrix(T[][] matrix) {
        Class<T> elementType = (Class<T>) matrix.getClass().getComponentType().getComponentType();
        
        @SuppressWarnings("unchecked")
        T[][] clonedMatrix = (T[][]) Array.newInstance(elementType, matrix.length, 0);

        for (int rowIdx = 0; rowIdx < matrix.length; rowIdx++) {
            clonedMatrix[rowIdx] = (T[]) Array.newInstance(elementType, matrix[rowIdx].length);
            for (int colIdx = 0, colSize = matrix[rowIdx].length; colIdx < colSize; colIdx++) {
                clonedMatrix[rowIdx][colIdx] = matrix[rowIdx][colIdx];
            }
        }

        return clonedMatrix;
    }
    
    public static int countSymbolByType(Symbol[] listSymbol, SymbolType type) {
        int count = 0;
        for (Symbol symbol : listSymbol) {
            if (symbol != null && symbol.type() == type) {
                count++;
            }
        }
        return count;
    }
    
    public static int countSymbolByType(Collection<Symbol> listSymbol, SymbolType type) {
        return (int) listSymbol.stream().filter( (symbol) -> symbol.type() == type).count();
    }
    
    public static int countSymbolByCode(Collection<Symbol> listSymbol, String code) {
        return (int) listSymbol.stream().filter( (symbol) -> symbol.code().equals(code)).count();
    }
    
    public static int countSymbolByType(Symbol[][] matrix, SymbolType type) {
        int count = 0;
        for(Symbol[] rowSymbol : matrix) {
            for (Symbol symbol : rowSymbol) {
                if (symbol != null && symbol.type() == type) {
                    count++;
                }
            }
        }
        return count;
    }

    public static List<String> convertToMatrixCode1DByReel(DataCell<? extends ISymbol>[][] matrix, List<Integer> tableFormat) {
        List<String> matrixCode1D = new ArrayList<>();
        
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    if (rowIdx < tableFormat.get(reelIdx)) {
                        if (matrix[rowIdx][reelIdx] != null && matrix[rowIdx][reelIdx].symbol() != null) {
                            matrixCode1D.add(matrix[rowIdx][reelIdx].symbol().code());
                        } else {
                            matrixCode1D.add("");
                        }
                    }
                }
            }
        }
        return matrixCode1D;
    }
    
    public static List<String> convertToMatrixID1DByReel(DataCell<? extends ISymbol>[][] matrix, List<Integer> tableFormat) {
        List<String> matrixCode1D = new ArrayList<>();
        
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    if (rowIdx < tableFormat.get(reelIdx)) {
                        if (matrix[rowIdx][reelIdx] != null && matrix[rowIdx][reelIdx].symbol() != null) {
                            matrixCode1D.add(String.valueOf(matrix[rowIdx][reelIdx].symbol().id()));
                        } else {
                            matrixCode1D.add("");
                        }
                    }
                }
            }
        }
        return matrixCode1D;
    }
    
    public static List<List<String>> convertToListMatrixCode1DByReel(List<DataCell<Symbol>[][]> listMatrix, List<Integer> tableFormat) {
        if (listMatrix == null || listMatrix.size() == 0) {
            return null;
        } else {
            List<List<String>> list1DMatrix = new ArrayList<>();
            
            for(DataCell<Symbol>[][] matrix : listMatrix) {
                if ( matrix != null) {
                    
                    list1DMatrix.add(convertToMatrixCode1DByReel(matrix, tableFormat));
                } else {
                    list1DMatrix.add(null);
                }
            }
            
            return list1DMatrix;
        }
    }
    
    public static void removeSymbolByType(List<Symbol> symbolList, SymbolType... symbolTypeList) {
        for (SymbolType symbolType : symbolTypeList) {
            symbolList.removeIf(symbol -> symbol.type().equals(symbolType));
        }
    }

    public static Symbol[] removeSymbolByTye(Symbol[] symbolArr, SymbolType... symbolTypeList) {
        List<Symbol> symbolList = new LinkedList<Symbol>(Arrays.asList(symbolArr));
        for (SymbolType symbolType : symbolTypeList) {
            removeSymbolByType(symbolList, symbolType);
        }
        return symbolList.toArray(new Symbol[symbolList.size()]);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T[] addElementToArray(T[] arr, T... eles) {
        List<T> list = new LinkedList<>(Arrays.asList(arr));
        list.addAll(Arrays.asList(eles));
        
        return list.toArray((T[]) Array.newInstance(arr.getClass().getComponentType(), list.size()));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> DataCell<T>[][] initVerticalDataCellMatrix( Class<T> clazzzTypeSymbol, List<Integer> tableFormat) {
        DataCell.DataCellBuilder<T> builder = DataCell.builder(clazzzTypeSymbol);
        
        return initVerticalMatrix(builder.build().getClass(), tableFormat);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> DataCell<T>[][] initDataCellMatrix( Class<T> clazzzTypeSymbol, int... dimensions) {
        DataCell.DataCellBuilder<T> builder = DataCell.builder(clazzzTypeSymbol);
        
        return initVerticalMatrix(builder.build().getClass(), dimensions);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> DataCell<T>[] initDataCellArray(Class<T> clazzzTypeSymbol, int len) {
        DataCell.DataCellBuilder<T> builder = DataCell.builder(clazzzTypeSymbol);
        
        return (DataCell<T>[]) Array.newInstance(builder.build().getClass(), len);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> DataCell<T>[][] convertToAbstractMatrixDataCell(Class<T> abstractClass, DataCell<? extends T>[][] matrixDataCell) {
        if (matrixDataCell == null) {
            return null;
        }
        
        DataCell<T>[][] matrixAbstractDataCell = MatrixUtil.initDataCellMatrix(abstractClass, matrixDataCell.length, 0);
        
        for (int rowIdx = 0, rowSize = matrixDataCell.length; rowIdx < rowSize; rowIdx++) {
            matrixAbstractDataCell[rowIdx] = MatrixUtil.initDataCellArray(abstractClass, matrixDataCell[rowIdx].length);
            for (int colIdx = 0, colSize = matrixDataCell[rowIdx].length; colIdx < colSize; colIdx++) {
                matrixAbstractDataCell[rowIdx][colIdx] = (DataCell<T>) matrixDataCell[rowIdx][colIdx];
            }
        }
        return matrixAbstractDataCell;
    }
    
    public static <T> DataCell<? extends T>[][] convertToConcreteMatrixDataCell(Class<? extends T> concreteClass, DataCell<T>[][] matrixDataCell) {
        if (matrixDataCell == null) {
            return null;
        }
        
        DataCell<? extends T>[][] matrixAbstractDataCell = MatrixUtil.initDataCellMatrix(concreteClass, matrixDataCell.length, 0);
        
        for (int rowIdx = 0, rowSize = matrixDataCell.length; rowIdx < rowSize; rowIdx++) {
            matrixAbstractDataCell[rowIdx] = MatrixUtil.initDataCellArray(concreteClass, matrixDataCell[rowIdx].length);
            for (int colIdx = 0, colSize = matrixDataCell[rowIdx].length; colIdx < colSize; colIdx++) {
                matrixAbstractDataCell[rowIdx][colIdx] = (DataCell<? extends T>) matrixDataCell[rowIdx][colIdx];
            }
        }
        return matrixAbstractDataCell;
    }
    
    public static void logMatrix(DataCell<? extends ISymbol>[][] matrix, String matrixName, List<Integer> tableFormat) {
        log.info("=======Matrix {}======", matrixName);
        
        if ( matrix == null)
            return;
        
        StringBuilder sb;
        for (DataCell<? extends ISymbol>[] rowSymbol : matrix) {
            sb = new StringBuilder();
            for (DataCell<? extends ISymbol> dataCell : rowSymbol) {
                int size = 3;
                if (dataCell != null) {
                    size = dataCell.symbol().code().length();
                    sb.append(dataCell.symbol().code());
                } else {
                    sb.append(" - ");
                }
                for (int k = 0; k < 5 - size; k++)
                    sb.append(" ");
                sb.append("| ");
            }
            log.info(sb.toString());
        }
        logMatrix1D(matrix, tableFormat);
    }
    
    public static void logMatrixTest(DataCell<? extends ISymbol>[][] matrix, String matrixName, List<Integer> tableFormat) {
        log.info("=======Matrix {}======", matrixName);
        
        if ( matrix == null)
            return;
        
        StringBuilder sb;
        for (DataCell<? extends ISymbol>[] rowSymbol : matrix) {
            sb = new StringBuilder();
            for (DataCell<? extends ISymbol> dataCell : rowSymbol) {
                if (dataCell != null) {
                    sb.append(dataCell.symbol().code());
                } else {
                    sb.append(" ");
                }
                sb.append(" | ");
            }
            log.info(sb.toString());
        }
    }

    public static void logMatrix1D(DataCell<? extends ISymbol>[][] matrix, List<Integer> tableFormat) {
        log.info(MatrixUtil.convertToMatrixCode1DByReel(matrix, tableFormat).toString().replaceAll(" ", ""));
    }
    
    public static void logMatrixRTP(DataCell<? extends ISymbol>[][] matrix, String matrixName, List<Integer> tableFormat) {
        log.error("=======Matrix RTP {}======", matrixName);
        
        if ( matrix == null)
            return;
        
        StringBuilder sb;
        for (DataCell<? extends ISymbol>[] rowSymbol : matrix) {
            sb = new StringBuilder();
            for (DataCell<? extends ISymbol> dataCell : rowSymbol) {
                if (dataCell != null) {
                    sb.append(dataCell.symbol().code());
                } else {
                    sb.append(" ");
                }
                sb.append(" | ");
            }
            log.error(sb.toString());
        }
    }
    
    public static boolean isMatrixEmpty(DataCell[][] matrix) {
        if (matrix == null) 
            return true;
        if (matrix[0].length == 0)
            return true;
        return false;
    }
    
    public static List<String> convertToMatrixVerticalCode1DByReel(DataCell<? extends ISymbol>[][] matrix, List<Integer> tableFormat) {
        List<String> matrixCode1D = new ArrayList<>();
        
        if (matrix != null) {
            for (int rowIdx = 0; rowIdx < tableFormat.get(0); rowIdx++) {
                for (int reelIdx = 0; reelIdx < tableFormat.size(); reelIdx++) {
                    matrixCode1D.add(matrix[rowIdx][reelIdx].symbol().code());
                }
            }
        }
        return matrixCode1D;
    }
    //support cluster -- start region
    public static List<DataCell<Symbol>> convertToMatrixSymbol1DByReel(DataCell<? extends ISymbol>[][] matrix, List<Integer> tableFormat) {
        List<DataCell<Symbol>> matrixCode1D = new ArrayList<>();
        
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    if (rowIdx < tableFormat.get(reelIdx)) {
                        if (matrix[rowIdx][reelIdx] != null && matrix[rowIdx][reelIdx].symbol() != null) {
                            matrixCode1D.add(DataCell.builder(Symbol.class)
                                .symbol((Symbol)matrix[rowIdx][reelIdx].symbol())
                                .vertex(matrix[rowIdx][reelIdx].vertex())
                                .check(matrix[rowIdx][reelIdx].check()).build());
                        } 
                    }
                }
            }
        }
        return matrixCode1D;
    }
    
    public static DataCell<Symbol>[][] updateVertexInMatrix(DataCell<Symbol>[][] matrix, List<Integer> tableFormat) {
        if (matrix != null) {
            int vertexIndex = 0;
            for (int col = 0; col < tableFormat.size(); col++) {
                for (int row = 0; row < tableFormat.get(col); row++) {
                    if (row < matrix.length && col < matrix[row].length) {
                        matrix[row][col] = matrix[row][col].toBuilder().symbol(matrix[row][col].symbol()).vertex(vertexIndex++).check(false).build();
                    }
                }
            }
            
        }
        return matrix;
    }
    
    public static DataCell<Symbol>[][] convertMatrix1DToMatrix2D(List<DataCell<Symbol>> matrix1DVertex, List<Integer> tableFormat){
        DataCell<Symbol>[][] newMatrix = MatrixUtil.reverseMatrix(MatrixUtil.initVerticalDataCellMatrix(Symbol.class, tableFormat));
        int in1D = 0;
        for (int col = 0; col < tableFormat.size(); col++) {
            for (int row = 0; row < tableFormat.get(col); row++) {
                newMatrix[row][col] = matrix1DVertex.get(in1D).toBuilder()
                        .symbol(matrix1DVertex.get(in1D).symbol())
                        .check(matrix1DVertex.get(in1D).check())
                        .vertex(matrix1DVertex.get(in1D).vertex()).build();
                in1D ++;
            }
        }
        return newMatrix;
    }
    // end support cluster 
}
