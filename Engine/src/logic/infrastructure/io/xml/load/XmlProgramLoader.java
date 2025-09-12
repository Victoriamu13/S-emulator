package logic.infrastructure.io.xml.load;

import logic.infrastructure.io.xml.build.XmlProgramBuilder;
import logic.infrastructure.io.xml.parser.ParseResult;
import logic.infrastructure.io.xml.parser.XmlProgramParser;
import logic.infrastructure.io.xml.parser.utils.BasicFileChecks;
import logic.infrastructure.io.xml.validation.ValidateResult;
import logic.infrastructure.io.xml.validation.XmlProgramValidator;
import logic.domain.program.SProgram;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XmlProgramLoader {
    private final XmlProgramParser parser = new XmlProgramParser();
    private final XmlProgramValidator validator = new XmlProgramValidator();
    private final XmlProgramBuilder builder = new XmlProgramBuilder();


    public LoadResult load(Path xmlPath) {

        //BasicFileChecks
        List<String> errors = new ArrayList<>(BasicFileChecks.validate(xmlPath));
         if (!errors.isEmpty()) {
            return LoadResult.failed(errors);
         }

         // 1) Parse
         ParseResult parsed = parser.parse(xmlPath);
         if (!parsed.errors().isEmpty()) {
             return LoadResult.failed(parsed.errors());
         }

         // 2) Validate
         ValidateResult validated = validator.validate(parsed.programName(), parsed.raw());
         if (!validated.errors().isEmpty()) {
             return LoadResult.failed(validated.errors());
         }

        SProgram program = builder.build(parsed.programName(), parsed.raw());
        return LoadResult.success(program);
    }
}