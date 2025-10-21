package logic.infrastructure.io.xml.load;

import logic.domain.program.functions.FunctionRepository;
import logic.infrastructure.io.xml.build.XmlProgramBuilder;
import logic.infrastructure.io.xml.parser.programParse.ProgramParseResult;
import logic.infrastructure.io.xml.parser.programParse.XmlProgramParser;
import logic.infrastructure.io.xml.parser.utils.BasicFileChecks;
import logic.infrastructure.io.xml.validation.ValidateResult;
import logic.infrastructure.io.xml.validation.XmlProgramValidator;
import logic.domain.program.SProgram;
import logic.system.programs.functions.repository.GlobalFunctionRepository;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class XmlProgramLoader {
    private final XmlProgramParser parser = new XmlProgramParser();
    private final XmlProgramValidator validator = new XmlProgramValidator();
    private final XmlProgramBuilder builder = new XmlProgramBuilder();


    public LoadResult load(InputStream inputStream) {
        try {
            // 1) BasicFileChecks
            List<String> errors = new ArrayList<>(BasicFileChecks.validate(inputStream));
            if (!errors.isEmpty()) return LoadResult.failed(errors);

            // 2) Parse
            ProgramParseResult parsed = parser.parse(inputStream);
            if (!parsed.errors().isEmpty()) return LoadResult.failed(parsed.errors());

            // 3) Validate
            ValidateResult validated = validator.validateStructure(parsed.programName(), parsed.raw(), parsed.functions());
            if (!validated.errors().isEmpty()) return LoadResult.failed(validated.errors());

            // 4) Build program
            SProgram program = builder.build(parsed.programName(), parsed.raw());


            // 5) Build repository of functions
            FunctionRepository repo = new FunctionRepository();
            program.setFunctionLookup(repo);
            builder.registerFunctions(parsed.functions(), repo);

            return LoadResult.success(program, repo);
        } catch (Exception e) {
            return LoadResult.failed(List.of("Failed to load from stream: " + e.getMessage()));
        }
    }
}